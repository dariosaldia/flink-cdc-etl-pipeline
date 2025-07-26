package com.example.flinkcdc.config;

import java.util.Optional;

public record StorageConfig(
    String outputPath,
    Optional<StorageS3Config> s3
) { }
