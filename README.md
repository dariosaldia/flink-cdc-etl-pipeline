# General Overview

This project demonstrates a **real-time data pipeline** that continuously updates a data lake with changes from a transactional database using Change Data Capture (CDC).

Specifically, CDC events captured from a SQL database via Debezium are streamed into Kafka. Apache Flink consumes these events from Kafka to perform transformation, filtering, and enrichment of the raw change data.

The processed data is written as partitioned **Parquet** files to an S3-compatible data lake bucket, enabling efficient storage and querying.

Key features implemented include:

- Continuous, low-latency ingestion of CDC events from Kafka
- Exactly-once processing semantics with Flink checkpointing
- Event-time processing with watermarks to handle out-of-order and late events
- Writing partitioned Parquet files directly to S3-compatible storage

This pipeline provides a modern streaming alternative to traditional batch ETL jobs, enabling fresher data availability and faster analytics.

## Infrastructure Components

This Docker Compose stack brings up the following services:

- **MySQL**
  A MySQL 8.0 database running on port 3306, initialized with an `inventory.orders` table and a `debezium` user. Includes a healthcheck to wait until the database is ready for connections.
- **Kafka (KRaft mode)**
  A single-node Kafka broker in KRaft (ZooKeeper-less) mode:

  - **PLAINTEXT** listener on port 9092 for host-side clients
  - **PLAINTEXT_INTERNAL** listener on port 29092 for intra-Docker traffic
    Automatically creates an `orders_cdc` topic (3 partitions, RF=1) and includes a broker healthcheck.
- **Kafka Connect (Debezium)**
  Debezium’s Kafka Connect image on port 8083, configured to consume MySQL binlog events and publish CDC records to Kafka topics. Waits for both MySQL and Kafka to be healthy before starting, and exposes a REST endpoint for connector management.
- **Connector Initializer**
  A lightweight container that waits for the Connect REST API, then registers the MySQL-to-Kafka connector using `register-orders-connector.json`. Runs once at startup to wire up the CDC pipeline.
