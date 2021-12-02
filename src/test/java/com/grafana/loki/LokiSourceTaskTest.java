package com.grafana.loki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.undertow.Undertow;
import io.undertow.util.Headers;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LokiSourceTaskTest {
  Undertow server =
      Undertow.builder()
          .addHttpListener(3100, "localhost")
          .setHandler(
              exchange -> {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                final InputStream input =
                    Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("result.json");
                final String response = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                exchange.getResponseSender().send(response);
              })
          .build();

  @BeforeAll
  void startLokiMock() {
    server.start();
  }

  @AfterAll
  void stopLokiMock() {
    server.stop();
  }

  @Test
  void poll() {
    final LokiSourceTask task = new LokiSourceTask();
    Map<String, String> props =
        new HashMap<String, String>() {
          {
            put(LokiSourceConnector.ENDPOINT_CONFIG, "http://localhost:3100");
            put(LokiSourceConnector.QUERY_CONFIG, "{job=\"docker\"}");
            put(LokiSourceConnector.TOPIC_CONFIG, "my_topic");
          }
        };
    task.start(props);

    var records = task.poll();
    assertThat(records, hasSize(100));
  }
}
