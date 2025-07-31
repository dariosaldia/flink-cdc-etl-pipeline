package com.example.flinkcdc.flink;

import com.example.flinkcdc.config.AppConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public final class FlinkEnvFactory {

    private FlinkEnvFactory() {
    }

    public static StreamExecutionEnvironment create(AppConfig cfg) {
        boolean isLocal = cfg.flink()
                .runMode()
                .map("local"::equalsIgnoreCase)
                .orElse(false);

        StreamExecutionEnvironment env;
        if (isLocal) {
            Configuration flinkConf = new Configuration();
            applyLocalSettings(cfg, flinkConf);
            Configuration globalFlinkConf = GlobalConfiguration.loadConfiguration(flinkConf);
            FileSystem.initialize(globalFlinkConf, null);
            env = StreamExecutionEnvironment.createLocalEnvironment(globalFlinkConf);
        } else {
            env = StreamExecutionEnvironment.getExecutionEnvironment();
        }

        env.enableCheckpointing(cfg.flink().checkpointingIntervalMs());
        return env;
    }

    private static void applyLocalSettings(AppConfig cfg, Configuration flinkConf) {
        cfg.flink()
                .uiPort()
                .ifPresent(port -> flinkConf.set(RestOptions.PORT, port));

        cfg.flink()
                .parallelismDefault()
                .ifPresent(par -> flinkConf.set(CoreOptions.DEFAULT_PARALLELISM, par));

        cfg.storage()
                .s3()
                .ifPresent(s3 -> {
                    flinkConf.setString("s3.access-key", s3.s3AccessKey());
                    flinkConf.setString("s3.secret-key", s3.s3SecretKey());
                    flinkConf.setString("s3.endpoint", s3.s3Endpoint());
                    flinkConf.setString("s3.path.style.access", s3.s3PathStyleAccess());
                });
    }
}
