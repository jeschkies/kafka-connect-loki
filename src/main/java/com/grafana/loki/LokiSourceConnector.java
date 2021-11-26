package com.grafana.loki;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

public class LokiSourceConnector extends SourceConnector {
  public static final String ENDPOINT_CONFIG = "endpoint";
  public static final String USERNAME_CONFIG = "username";
  public static final String PASSWORD_CONFIG = "password";
  public static final String QUERY_CONFIG = "query";
  public static final String TOPIC_CONFIG = "topic";
  public static final String START_CONFIG = "start";

  private String topic;

  @Override
  public ConfigDef config() {
    return null;
  }

  @Override
  public void start(Map<String, String> props) {
    topic = props.get(TOPIC_CONFIG);
  }

  @Override
  public void stop() {}

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    // TODO: Determine how many tasks we have. For now we do not support sharding.
    return Collections.emptyList();
  }

  @Override
  public Class<? extends Task> taskClass() {
    return LokiSourceTask.class;
  }

  @Override
  public String version() {
    return null;
  }
}
