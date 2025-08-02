package com.example.flinkcdc.pipeline.aggregate;

import com.example.flinkcdc.model.Order;
import org.apache.flink.api.common.functions.AggregateFunction;

public class OrderStatsAggregator implements AggregateFunction<Order, OrderStatsAccumulator, OrderStatsAccumulator> {
    @Override
    public OrderStatsAccumulator createAccumulator() {
        return new OrderStatsAccumulator();
    }

    @Override
    public OrderStatsAccumulator add(Order order, OrderStatsAccumulator accumulator) {
        accumulator.totalQuantity += order.quantity();
        accumulator.count += 1;
        return accumulator;
    }

    @Override
    public OrderStatsAccumulator getResult(OrderStatsAccumulator accumulator) {
        return accumulator;
    }

    @Override
    public OrderStatsAccumulator merge(OrderStatsAccumulator a, OrderStatsAccumulator b) {
        a.totalQuantity += b.totalQuantity;
        a.count += b.count;
        return a;
    }
}
