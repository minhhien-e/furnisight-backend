package com.furnisight.cart.dto.event;

import java.util.List;

public record ProductUpdatedEvent(
        String productId,
        String name,
        String slug,
        String imageUrl,
        String status,
        List<ProductUpdatedVariantEvent> variants
) {
    public record ProductUpdatedVariantEvent(
            String id,
            Double price,
            Integer stockQuantity,
            Double length,
            Double width,
            Double height,
            Double weight,
            String color,
            String material,
            String warranty
    ) {
    }
}
