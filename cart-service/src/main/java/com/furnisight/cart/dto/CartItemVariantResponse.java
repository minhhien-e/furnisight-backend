package com.furnisight.cart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemVariantResponse {
    private String id;
    private Double price;
    private Integer stockQuantity;
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private String color;
    private String material;
    private String warranty;
}
