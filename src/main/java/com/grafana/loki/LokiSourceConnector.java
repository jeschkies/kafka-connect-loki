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

  private static final ConfigDef CONFIG_DEF =
      new ConfigDef()
          .define(
              ENDPOINT_CONFIG,
              ConfigDef.Type.STRING,
              ConfigDef.Importance.HIGH,
              "The Loki endpoint")
          .define(
              USERNAME_CONFIG,
              ConfigDef.Type.STRING,
              ConfigDef.Importance.LOW,
              "The Loki user name")
          .define(
              PASSWORD_CONFIG,
              ConfigDef.Type.STRING,
              ConfigDef.Importance.LOW,
              "The Loki user password")
          .define(QUERY_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The Loki query")
          .define(
              TOPIC_CONFIG,
              ConfigDef.Type.STRING,
              ConfigDef.Importance.HIGH,
              "The topic to publish data to");

  private String topic;

  @Override
  public ConfigDef config() {
    return CONFIG_DEF;
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
