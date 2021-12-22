#!/usr/bin/env sh
docker-compose exec kcat curl -X PUT -H "Content-Type: application/json" \
    http://kafka-connect:8083/connectors/log-gen/config \
     -d '{
          "connector.class":"com.grafana.loki.LokiSourceConnector",
          "endpoint": "loki:3100",
          "username": "",
          "password": "",
          "query": "{job=\"docker\"}",
          "topic": "loki"
       }'

# docker-compose exec kcat kcat -C -b broker:29092 -t loki