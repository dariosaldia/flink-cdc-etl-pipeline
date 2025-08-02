package com.example.flinkcdc.sink;

import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.MemorySize.MemoryUnit;
import org.apache.flink.streaming.api.functions.sink.filesystem.PartFileInfo;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * A rolling policy that:
 * 1) Rolls on every checkpoint (required for bulk formats)
 * 2) Also rolls when the file grows too large
 * 3) Also rolls when the file is idle for too long
 */
public class CheckpointSizeTimeRollingPolicy<T> extends CheckpointRollingPolicy<T, String> {

    private final DefaultRollingPolicy<T, String> delegate;

    public CheckpointSizeTimeRollingPolicy(
            long maxPartSizeBytes,
            long rolloverIntervalMs,
            long inactivityIntervalMs
    ) {
        this.delegate = DefaultRollingPolicy.builder()
                .withMaxPartSize(MemorySize.parse(String.valueOf(maxPartSizeBytes), MemoryUnit.BYTES))
                .withRolloverInterval(Duration.of(rolloverIntervalMs, ChronoUnit.MILLIS))
                .withInactivityInterval(Duration.of(inactivityIntervalMs, ChronoUnit.MILLIS))
                .build();
    }

    @Override
    public boolean shouldRollOnEvent(PartFileInfo<String> partFileInfo, T event)
            throws IOException {
        return delegate.shouldRollOnEvent(partFileInfo, event);
    }

    @Override
    public boolean shouldRollOnProcessingTime(PartFileInfo<String> partFileInfo, long currentProcessingTime)
            throws IOException {
        return delegate.shouldRollOnProcessingTime(partFileInfo, currentProcessingTime);
    }
}
