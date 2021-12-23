# Kafka Loki Connector Demo

The demo is run via Docker Compose. The compose file is at the root of this project.
All commands must be run from the root.

1. Compile the plugin with `./gradlew shadowJar`. This will place the "uber"-JAR in `build/libs`.
2. Start the cluster with `docker-compose up`. This will start Loki, Grafana, Promtail, a log generator, a Kafka broker
   and Kafka connect.
3. Create a Kafka connection with `./demo/create-connector.sh`.
4. Follow this newly created `loki` topic with `docker-compose exec shell kcat -C -b broker:29092 -t loki`.

*Credits:*
The demo is based on [From Zero to Hero with Kafka Connect](https://github.com/confluentinc/demo-scene/tree/master/kafka-connect-zero-to-hero)
and [MongoDB Kafka Connect Quickstart](https://docs.mongodb.com/kafka-connector/current/quick-start/).