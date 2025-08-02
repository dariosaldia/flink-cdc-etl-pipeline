package com.example.flinkcdc.pipeline.aggregate;

import com.example.flinkcdc.model.stats.OrderStats;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class OrderStatsWindowFunction extends ProcessWindowFunction<OrderStatsAccumulator, OrderStats, Integer, TimeWindow> {
    @Override
    public void process(Integer productId, Context context, Iterable<OrderStatsAccumulator> accIterator, Collector<OrderStats> out) throws Exception {
        OrderStatsAccumulator acc = accIterator.iterator().next();
        OrderStats stats = new OrderStats(productId, context.window().getStart(), context.window().getEnd(), acc.totalQuantity, acc.count);
        out.collect(stats);
    }
}
