package com.example.flinkcdc.monitoring;

import com.example.flinkcdc.model.Order;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.metrics.Counter;

public class OrderMetricMapper extends RichMapFunction<Order, Order> {

  private transient Counter processedCounter;

  @Override
  public void open(OpenContext ctx) throws Exception {
    this.processedCounter =
        getRuntimeContext()
            .getMetricGroup()
            .addGroup("orders")
            .counter("processed");
  }

  @Override
  public Order map(Order order) {
    processedCounter.inc();
    return order;
  }
}

