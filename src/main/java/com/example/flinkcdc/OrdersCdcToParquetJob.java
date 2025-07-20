package com.example.flinkcdc;

import com.example.flinkcdc.config.AppConfig;
import com.example.flinkcdc.config.ConfigLoader;
import com.example.flinkcdc.model.CdcEvent;
import com.example.flinkcdc.model.Order;
import com.example.flinkcdc.serde.CdcEventDeserializationSchema;
import com.example.flinkcdc.sink.CheckpointSizeTimeRollingPolicy;
import com.example.flinkcdc.sink.OrderDateBucketAssigner;
import java.time.Duration;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.parquet.avro.AvroParquetWriters;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class OrdersCdcToParquetJob {

  public static void main(String[] args) throws Exception {
    AppConfig cfg = ConfigLoader.load();

    StreamExecutionEnvironment env = cfg.flink().runMode()
        .filter("local"::equalsIgnoreCase)
        .map(__ -> cfg.flink().uiPort()
            .map(port -> {
              Configuration conf = new Configuration();
              conf.set(RestOptions.PORT, port);
              return StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
            })
            .orElseGet(() -> StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(
                new Configuration()))
        )
        .orElseGet(StreamExecutionEnvironment::getExecutionEnvironment);
    env.enableCheckpointing(cfg.flink().checkpointingIntervalMs());

    KafkaSource<CdcEvent> source = KafkaSource.<CdcEvent>builder()
        .setBootstrapServers(
            cfg.kafka().inBootstrapServers()
        )
        .setTopics(cfg.kafka().inTopics())
        .setGroupId(cfg.kafka().inGroupId())
        .setStartingOffsets(OffsetsInitializer.latest())
        .setDeserializer(new CdcEventDeserializationSchema())
        .build();

    DataStream<CdcEvent> raw = env.fromSource(
        source,
        WatermarkStrategy
            .<CdcEvent>forBoundedOutOfOrderness(
                Duration.ofSeconds(cfg.kafka().maxOutOfOrderSecs())
            )
            .withTimestampAssigner(
                (event, ts) -> System.currentTimeMillis()
            ),
        "orders-cdc-source"
    );

    DataStream<Order> orders = raw
        .filter(evt -> !"d".equals(evt.op()))
        .map(CdcEvent::after);

    FileSink<Order> sink = FileSink
        .forBulkFormat(
            new Path(cfg.storage().outputPath()),
            AvroParquetWriters.forReflectRecord(Order.class)
        )
        .withBucketAssigner(new OrderDateBucketAssigner())
        .withRollingPolicy(new CheckpointSizeTimeRollingPolicy(cfg.flink().maxPartSizeBytes(),
            cfg.flink().rolloverIntervalMs(), cfg.flink().inactivityIntervalMs()))
        .build();

    orders.sinkTo(sink);
    env.execute("Orders CDC => Parquet on FS");
  }
}
