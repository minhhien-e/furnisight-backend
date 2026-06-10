package com.furnisight.order.domain.services.dto;

@lombok.Data
@lombok.Builder
public class OrderItemParam {
    private String productId;
    private String variantId;
    private String categoryName;
    private String productName;
    private String color;
    private String material;
    private String warranty;
    private com.furnisight.order.domain.valueobjects.ProductDimensions dimensions;
    private Double price;
    private Integer quantity;
    private String imageUrl;
}
