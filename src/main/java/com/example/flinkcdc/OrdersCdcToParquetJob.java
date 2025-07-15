package com.example.flinkcdc;

import com.example.flinkcdc.config.AppConfig;
import com.example.flinkcdc.config.ConfigLoader;
import java.io.IOException;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class OrdersCdcToParquetJob {

    public static void main(String[] args) throws IOException {
        AppConfig appConfig = ConfigLoader.load();

        System.out.println(appConfig);

//        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
//        executionEnvironment.enableCheckpointing(10_000L);
    }
}
