package com.example.flinkcdc.sink;

import com.example.flinkcdc.model.Order;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;

public class OrderDateBucketAssigner implements BucketAssigner<Order, String> {

  @Override
  public String getBucketId(Order order, Context context) {
    return "order_date=" + order.orderDate();
  }

  @Override
  public SimpleVersionedSerializer<String> getSerializer() {
    return SimpleVersionedStringSerializer.INSTANCE;
  }
}
