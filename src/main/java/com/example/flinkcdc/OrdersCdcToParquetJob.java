package com.example.flinkcdc;

import com.example.flinkcdc.config.AppConfig;
import com.example.flinkcdc.config.ConfigLoader;
import com.example.flinkcdc.flink.FlinkEnvFactory;
import com.example.flinkcdc.pipeline.OrdersPipeline;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class OrdersCdcToParquetJob {

    public static void main(String[] args) throws Exception {
        AppConfig cfg = ConfigLoader.load();
        System.out.println(cfg);

        StreamExecutionEnvironment env = FlinkEnvFactory.create(cfg);

        OrdersPipeline.build(env, cfg);
        env.execute("Orders CDC => Parquet on FS");
    }
}
