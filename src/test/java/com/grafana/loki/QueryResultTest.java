package com.grafana.loki;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

public class QueryResultTest {
  @Test
  void deserialize() throws IOException {
    final InputStream input = IOUtils.toInputStream("{}", Charset.defaultCharset());
    QueryResult result = QueryResult.fromJSON(input);
    QueryResult expected = new QueryResult();
    QueryResult.Data data = new QueryResult.Data();
    data.setStreams(Collections.emptyList());
    expected.setData(data);

    // TODO: use hamcrest
    assertEquals(expected.getData().getStreams().size(), result.getData().getStreams().size());
  }
}
