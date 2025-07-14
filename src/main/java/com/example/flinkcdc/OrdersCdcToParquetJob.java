package com.example.flinkcdc;

import com.example.flinkcdc.config.AppConfig;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import java.io.File;
import java.io.IOException;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class OrdersCdcToParquetJob {

    public static void main(String[] args) throws IOException {
        AppConfig appConfig = new TomlMapper()
            .readerFor(AppConfig.class)
            .readValue(new File("config.toml"));

        System.out.println(appConfig);

//        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
//        executionEnvironment.enableCheckpointing(10_000L);
    }
}
