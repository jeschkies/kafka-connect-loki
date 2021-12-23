package com.grafana.loki;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
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
  private Long lastTimestamp = 0L;

  private String authHeader;

  @Override
  public String version() {
    return new LokiSourceConnector().version();
  }

  @Override
  public void start(Map<String, String> props) {
    lokiEndpoint = props.get(LokiSourceConnector.ENDPOINT_CONFIG);
    lokiQuery = props.get(LokiSourceConnector.QUERY_CONFIG);
    topic = props.get(LokiSourceConnector.TOPIC_CONFIG);

    if (props.containsKey(LokiSourceConnector.START_CONFIG)) {
      lastTimestamp = Long.parseLong(props.get(LokiSourceConnector.START_CONFIG));
    } else {
      lastTimestamp = now() - ONE_HOUR;
    }

    final String user = props.getOrDefault(LokiSourceConnector.USERNAME_CONFIG, "");
    final String password = props.getOrDefault(LokiSourceConnector.PASSWORD_CONFIG, "");

    if (user.isEmpty() && password.isEmpty()) {
      authHeader =
          "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
    }
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
  public List<SourceRecord> poll() {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      final Long start = lastTimestamp + 1L;
      final Long end = now();
      final URI uri =
          new URIBuilder(lokiEndpoint + "/loki/api/v1/query_range")
              .addParameter("query", lokiQuery)
              .addParameter("start", start.toString())
              .addParameter("end", end.toString())
              .addParameter("direction", "forward")
              .build();

      HttpGet request = new HttpGet(uri);
      if (authHeader != null) {
        request.setHeader("Authorization", authHeader);
      }
      try (CloseableHttpResponse response = client.execute(request)) {
        final HttpEntity entity = response.getEntity();

        if (response.getCode() != 200) {
          try {
            final String content = EntityUtils.toString(entity);
            log.error("Could not fetch logs: {}:{}", response.getCode(), content);
            return null;
          } catch (ParseException e) {
            log.error("Could not read error message", e);
            return null;
          }
        }

        final InputStream content = entity.getContent();
        final QueryResult result = QueryResult.fromJSON(content);

        var records =
            result.getData().getStreams().stream()
                .map(QueryResult.Stream::getValues)
                .flatMap(Collection::stream)
                .map(
                    entry -> {
                      final String line = entry.getLine();
                      this.lastTimestamp = entry.getTs();
                      Map<String, String> sourcePartition = Collections.emptyMap();
                      return new SourceRecord(
                          sourcePartition,
                          offsetValue(this.lastTimestamp),
                          topic,
                          Schema.STRING_SCHEMA,
                          line);
                    })
                .collect(Collectors.toList());

        log.debug("Fetched {} records from {} to {}", records.size(), start, end);

        EntityUtils.consume(entity);
        return records;
      }

    } catch (IOException e) {
      log.error(String.format("Could not load batch from Loki at %s", lokiEndpoint), e);
    } catch (URISyntaxException e) {
      log.error("Could not build Loki query URI", e);
    }
    return null;
  }

  static long ONE_HOUR = Duration.ofHours(1).toNanos();

  private Long now() {
    // This precision is fine for now()us since we control the offset.
    var now = Instant.now();
    return now.getEpochSecond() * 1000000000L; // as nano seconds
  }

  private Map<String, Long> offsetValue(Long timestamp) {
    return Collections.singletonMap(TIMESTAMP_FIELD, timestamp);
  }
}
