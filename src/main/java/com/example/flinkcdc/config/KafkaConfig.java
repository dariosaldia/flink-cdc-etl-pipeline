package com.example.flinkcdc.config;

public record KafkaConfig(
  String inBootstrapServers,
  String inTopics,
  String inGroupId,
  long maxOutOfOrderSecs
){ }
