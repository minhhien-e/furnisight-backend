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

    private String slug;

    @Transient
    private Integer stockQuantity;

    private Double length;

    private Double width;

    private Double height;

    private Double weight;

    private String color;

    private String material;

    private String warranty;

    @Transient
    private List<CartItemVariant> variants;
}
