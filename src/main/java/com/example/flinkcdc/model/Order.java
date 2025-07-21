package com.example.flinkcdc.model;

import java.io.Serializable;

public record Order(
    int    orderId,
    String productName,
    int    quantity,
    String orderDate
) implements Serializable { }
