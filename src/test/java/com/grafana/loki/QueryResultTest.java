package com.grafana.loki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class QueryResultTest {
  @Test
  void deserialize() throws IOException {
    final InputStream input =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("result.json");
    QueryResult result = QueryResult.fromJSON(input);

    var values = result.getData().getStreams().get(0).getValues();
    assertThat(values, hasSize(100));
  }
}
