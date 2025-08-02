package com.example.flinkcdc.model.stats;

public record OrderStats(
        int productId,
        long windowStart,
        long windowEnd,
        long totalQuantity,
        long count
) { }
