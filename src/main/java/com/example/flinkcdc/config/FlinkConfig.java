package com.example.flinkcdc.config;

import java.util.Optional;

public record FlinkConfig (
    long checkpointingIntervalMs,
    long rolloverIntervalMs,
    long inactivityIntervalMs,
    long maxPartSizeBytes,
    Optional<String> runMode,
    Optional<Integer> uiPort
){}
