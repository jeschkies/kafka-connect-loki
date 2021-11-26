package com.grafana.loki;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class QueryResultTest {
  @Test
  void deserialize() throws IOException {
    final InputStream input =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("karsten.json");
    QueryResult result = QueryResult.fromJSON(input);

    // TODO: use hamcrest
    assertEquals(56, result.getData().getStreams().get(0).getValues().size());
  }
}
