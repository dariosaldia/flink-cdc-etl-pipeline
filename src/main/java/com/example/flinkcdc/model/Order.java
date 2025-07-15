package com.example.flinkcdc.model;

import java.io.Serializable;

public record Order(
    int    orderId,
    String productName,
    int    quantity,
    String orderDate,    // "YYYY-MM-DD"
    long   processedAt   // epoch millis
) implements Serializable { }
