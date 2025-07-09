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
