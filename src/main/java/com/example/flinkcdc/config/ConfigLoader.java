package com.example.flinkcdc.config;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

public class ConfigLoader {

  private static final String ENV_VAR = "CONFIG_FILE";
  private static final String DEFAULT_CONFIG_FILE = "config.toml";

  public static AppConfig load() throws IOException {
    TomlMapper mapper = new TomlMapper();

    String path = System.getenv(ENV_VAR);
    if (path == null || path.isBlank()) {
      path = DEFAULT_CONFIG_FILE;
      System.out.printf("[INFO] %s not set; loading default %s%n", ENV_VAR, path);
    } else {
      System.out.printf("[INFO] Loading config from %s%n", path);
    }

    // read and bind
    return mapper
        .readerFor(AppConfig.class)
        .readValue(new File(path));
  }
}
