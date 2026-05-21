package com.furnisight.cart.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemVariant {
    private String id;
    private Double price;
    private Double oldPrice;
    private Integer stockQuantity;
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private String color;
    private String material;
    private String warranty;
}
