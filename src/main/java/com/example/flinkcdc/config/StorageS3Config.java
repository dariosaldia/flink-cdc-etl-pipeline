package com.example.flinkcdc.config;

public record StorageS3Config(
    String s3Endpoint,
    String s3PathStyleAccess,
    String s3AccessKey,
    String s3SecretKey
) { }
