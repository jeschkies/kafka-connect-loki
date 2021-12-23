#!/usr/bin/env sh
docker-compose exec shell curl -X PUT -H "Content-Type: application/json" \
    http://kafka-connect:8083/connectors/log-gen/config \
     -d '{
          "connector.class":"com.grafana.loki.LokiSourceConnector",
          "endpoint": "http://loki:3100",
          "username": "",
          "password": "",
          "query": "{job=\"docker\"}",
          "topic": "loki"
       }'