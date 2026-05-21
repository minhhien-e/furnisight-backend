package com.furnisight.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private String variantId;
    private String name;
    private Double price;
    private String imageUrl;
    private Integer quantity;

    @Transient
    private String slug;

    @Transient
    private Double oldPrice;

    @Transient
    private Integer stockQuantity;

    @Transient
    private Double length;

    @Transient
    private Double width;

    @Transient
    private Double height;

    @Transient
    private Double weight;

    @Transient
    private String color;
}
