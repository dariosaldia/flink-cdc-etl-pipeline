package com.example.flinkcdc.model;

import java.io.Serializable;

public record Order(
        int orderId,
        int productId,
        int quantity,
        String orderDate
) implements Serializable {
}
