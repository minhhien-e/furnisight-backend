package com.furnisight.cart.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartItemResponse {
    private String productId;
    private String variantId;
    private String name;
    private String slug;
    private Double price;
    private String imageUrl;
    private Integer quantity;
    private Integer stockQuantity;
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private String color;
    private String material;
    private String warranty;
    private List<CartItemVariantResponse> variants;
}
