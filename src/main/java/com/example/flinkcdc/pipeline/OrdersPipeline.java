package com.example.flinkcdc.pipeline;

import com.example.flinkcdc.config.AppConfig;
import com.example.flinkcdc.model.CdcEvent;
import com.example.flinkcdc.model.Order;
import com.example.flinkcdc.model.stats.OrderStats;
import com.example.flinkcdc.monitoring.OrderMetricMapper;
import com.example.flinkcdc.pipeline.aggregate.OrderStatsAggregator;
import com.example.flinkcdc.pipeline.aggregate.OrderStatsWindowFunction;
import com.example.flinkcdc.serde.CdcEventDeserializationSchema;
import com.example.flinkcdc.sink.CheckpointSizeTimeRollingPolicy;
import com.example.flinkcdc.sink.OrderStatsBucketAssigner;
import com.example.flinkcdc.sink.OrderTimestampBucketAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.parquet.avro.AvroParquetWriters;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class OrdersPipeline {
    public static KafkaSource<CdcEvent> createSource(AppConfig cfg) {
        return KafkaSource.<CdcEvent>builder()
                .setBootstrapServers(cfg.kafka().inBootstrapServers())
                .setTopics(cfg.kafka().inTopics())
                .setGroupId(cfg.kafka().inGroupId())
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(new CdcEventDeserializationSchema())
                .build();
    }

    public static DataStream<CdcEvent> rawStream(StreamExecutionEnvironment env, AppConfig cfg) {
        return env.fromSource(
                createSource(cfg),
                WatermarkStrategy
                        .<CdcEvent>forBoundedOutOfOrderness(Duration.ofSeconds(cfg.kafka().maxOutOfOrderSecs()))
                        .withTimestampAssigner((e, ts) -> System.currentTimeMillis()),
                "orders-cdc-source"
        );
    }

    public static DataStream<Order> transform(DataStream<CdcEvent> raw) {
        return raw
                .filter(OrdersPipeline::isUpsert)
                .map(OrdersPipeline::toOrder)
                .map(new OrderMetricMapper());
    }

    public static FileSink<Order> createSink(AppConfig cfg) {
        return FileSink
                .forBulkFormat(new Path(cfg.storage().outputPath()),
                        AvroParquetWriters.forReflectRecord(Order.class))
                .withBucketAssigner(new OrderTimestampBucketAssigner())
                .withRollingPolicy(
                        new CheckpointSizeTimeRollingPolicy<>(
                                cfg.flink().maxPartSizeBytes(),
                                cfg.flink().rolloverIntervalMs(),
                                cfg.flink().inactivityIntervalMs()))
                .build();
    }

    public static FileSink<OrderStats> createStatsSink(AppConfig cfg) {
        String base = cfg.storage().outputPath();
        Path statsPath = new Path(base + "/stats/");

        return FileSink
                .forBulkFormat(
                        statsPath,
                        AvroParquetWriters.forReflectRecord(OrderStats.class))
                // bucket by window start time for easier partitioning:
                .withBucketAssigner(new OrderStatsBucketAssigner())
                .withRollingPolicy(
                        new CheckpointSizeTimeRollingPolicy<>(
                                cfg.flink().maxPartSizeBytes(),
                                cfg.flink().rolloverIntervalMs(),
                                cfg.flink().inactivityIntervalMs()))
                .build();
    }

    public static DataStream<OrderStats> aggregateWindowByProductId(
            DataStream<Order> orders,
            Duration windowSize
    ) {
        return orders
                .keyBy(Order::productId)
                .window(TumblingProcessingTimeWindows.of(Duration.of(windowSize.toMillis(), ChronoUnit.MILLIS)))
                .aggregate(
                        new OrderStatsAggregator(),
                        new OrderStatsWindowFunction()
                );
    }

    public static void build(StreamExecutionEnvironment env, AppConfig cfg) {
        DataStream<Order> orders = transform(rawStream(env, cfg));
        orders.sinkTo(createSink(cfg));
        DataStream<OrderStats> stats = aggregateWindowByProductId(orders, Duration.ofMillis(cfg.order().stats().windowSizeMs()));
        stats.sinkTo(createStatsSink(cfg));
    }

    static boolean isUpsert(CdcEvent evt) {
        return !"d".equals(evt.op());
    }

    static Order toOrder(CdcEvent evt) {
        return evt.after();
    }
}
