package com.grafana.loki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class LokiSourceTaskTest {
  @Test
  void poll() {
    final LokiSourceTask task = new LokiSourceTask();
    Map<String, String> props =
        new HashMap<String, String>() {
          {
            put(LokiSourceConnector.ENDPOINT_CONFIG, "https://logs-us-central.grafana.net");
            put(LokiSourceConnector.USERNAME_CONFIG, "7331");
            put(LokiSourceConnector.PASSWORD_CONFIG, "ey...");

            put(LokiSourceConnector.QUERY_CONFIG, "{host=\"babo\",job=\"systemd-journal\"}");
            put(LokiSourceConnector.TOPIC_CONFIG, "my_topic");
          }
        };
    task.start(props);

    var records = task.poll();
    assertThat(records, hasSize(9));
  }
}
