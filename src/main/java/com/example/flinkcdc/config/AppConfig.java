package com.example.flinkcdc.config;

public record AppConfig(FlinkConfig flink, KafkaConfig kafka, StorageConfig storage, OrderConfig order) {
}

