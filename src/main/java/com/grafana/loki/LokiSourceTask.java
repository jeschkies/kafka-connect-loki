package com.grafana.loki;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LokiSourceTask extends SourceTask {

  private static final Logger log = LoggerFactory.getLogger(LokiSourceTask.class);

  public static final String TIMESTAMP_FIELD = "timestamp";

  private String lokiEndpoint;
  private String lokiQuery;
  private String topic;
  private Long lastTimestamp;

  @Override
  public String version() {
    return new LokiSourceConnector().version();
  }

  @Override
  public void start(Map<String, String> props) {
    lokiEndpoint = props.get(LokiSourceConnector.ENDPOINT_CONFIG);
    lokiQuery = props.get(LokiSourceConnector.QUERY_CONFIG);
    topic = props.get(LokiSourceConnector.TOPIC_CONFIG);
  }

  @Override
  public void initialize(SourceTaskContext context) {
    super.initialize(context);
    lastTimestamp = 0L;
    context.offsetStorageReader().offset(Collections.emptyMap());
  }

  @Override
  public synchronized void stop() {}

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    try {
      final Long start = lastTimestamp + 1L;
      final URI uri =
          new URIBuilder(lokiEndpoint + "/loki/api/v1/query_range")
              .addParameter("query", lokiQuery)
              .addParameter("start", start.toString())
              .addParameter("end", now().toString())
              .addParameter("direction", "forward")
              .build();

      final InputStream content = Request.get(uri).execute().returnContent().asStream();
      final QueryResult result = QueryResult.fromJSON(content);

      var records = new ArrayList<SourceRecord>();
      for (QueryResult.Stream stream : result.getData().getStreams()) {
        for (QueryResult.LogEntry entry : stream.getValues()) {
          final String line = entry.getLine();
          Map<String, String> sourcePartition = Collections.emptyMap();
          var record =
              new SourceRecord(
                  sourcePartition,
                  offsetValue(this.lastTimestamp),
                  topic,
                  Schema.STRING_SCHEMA,
                  line);
          records.add(record);
        }
      }

      return records;
    } catch (IOException e) {
      log.error("Could not load batch from Loki", e);
    } catch (URISyntaxException e) {
      log.error("Could not build Loki query URI", e);
    }
    return null;
  }

  private Long now() {
    // This precision is fine for us since we control the offset.
    var now = Instant.now();
    return now.getEpochSecond() * 1000000000L; // as nano seconds
  }

  private Map<String, Long> offsetValue(Long timestamp) {
    return Collections.singletonMap(TIMESTAMP_FIELD, timestamp);
  }
}
