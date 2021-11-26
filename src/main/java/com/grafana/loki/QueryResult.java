package com.grafana.loki;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class QueryResult {

  static ObjectMapper mapper = new ObjectMapper();

  public static QueryResult fromJSON(InputStream input) throws IOException {
    return mapper.readValue(input, QueryResult.class);
  }

  public String getStatus() {
    return status;
  }

  public Data getData() {
    return data;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setData(Data data) {
    this.data = data;
  }

  private String status;
  private Data data;

  static class Data {
    public List<Stream> getStreams() {
      return streams;
    }

    public void setStreams(List<Stream> streams) {
      this.streams = streams;
    }

    private List<Stream> streams;
  }

  static class Stream {
    public Map<String, String> getLabels() {
      return labels;
    }

    public void setLabels(Map<String, String> labels) {
      this.labels = labels;
    }

    public List<LogEntry> getValues() {
      return values;
    }

    public void setValues(List<LogEntry> values) {
      this.values = values;
    }

    private Map<String, String> labels;
    private List<LogEntry> values;
  }

  static class LogEntry {
    public Long getTs() {
      return ts;
    }

    public void setTs(Long ts) {
      this.ts = ts;
    }

    public String getLine() {
      return line;
    }

    public void setLine(String line) {
      this.line = line;
    }

    private Long ts;
    private String line;
  }
}
