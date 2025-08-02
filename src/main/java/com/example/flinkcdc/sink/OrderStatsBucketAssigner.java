package com.example.flinkcdc.sink;

import com.example.flinkcdc.model.stats.OrderStats;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class OrderStatsBucketAssigner implements BucketAssigner<OrderStats, String> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    @Override
    public String getBucketId(OrderStats stats, Context context) {
        Instant instant = Instant.ofEpochMilli(stats.windowStart());
        String formatted = FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
        return "window_start=" + formatted;
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
