package com.example.flinkcdc.config;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public record FlinkConfig (
  long checkpointingIntervalMs
){}
