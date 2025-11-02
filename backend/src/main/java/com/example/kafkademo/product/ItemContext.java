package com.example.kafkademo.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// ItemContext.java
@Data
@Builder
public class ItemContext {
    private String itemId;
    private String itemType;
    private String category;
    private String sku;
    private String name;
    private BigDecimal price;
    private String currency;
    private int quantity;
}
