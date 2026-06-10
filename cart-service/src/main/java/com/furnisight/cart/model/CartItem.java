package com.furnisight.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.List;

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

    @Transient
    private String material;

    @Transient
    private String warranty;

    @Transient
    private List<CartItemVariant> variants;
}
