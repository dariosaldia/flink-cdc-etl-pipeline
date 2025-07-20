package com.example.flinkcdc.sink;

import com.example.flinkcdc.model.Order;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.MemorySize.MemoryUnit;
import org.apache.flink.streaming.api.functions.sink.filesystem.PartFileInfo;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

/**
 * A rolling policy that:
 *  1) Rolls on every checkpoint (required for bulk formats)
 *  2) Also rolls when the file grows too large
 *  3) Also rolls when the file is idle for too long
 */
public class CheckpointSizeTimeRollingPolicy extends CheckpointRollingPolicy<Order, String> {

  private final DefaultRollingPolicy<Order,String> delegate;

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
  public boolean shouldRollOnEvent(PartFileInfo<String> partFileInfo, Order order)
      throws IOException {
    return delegate.shouldRollOnEvent(partFileInfo, order);
  }

  @Override
  public boolean shouldRollOnProcessingTime(PartFileInfo<String> partFileInfo, long currentProcessingTime)
      throws IOException {
    return delegate.shouldRollOnProcessingTime(partFileInfo, currentProcessingTime);
  }
}
