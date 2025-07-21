package com.example.flinkcdc.sink;

import com.example.flinkcdc.model.Order;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;

public class OrderTimestampBucketAssigner extends DateTimeBucketAssigner<Order> {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Override
  public String getBucketId(Order order, Context context) {
    Instant instant = Instant.parse(order.orderDate());
    String date = FORMATTER.format(instant.atZone(ZoneId.systemDefault()).toLocalDate());
    return "order_date=" + date;
  }

}
