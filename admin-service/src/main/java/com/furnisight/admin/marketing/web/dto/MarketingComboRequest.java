package com.furnisight.admin.marketing.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MarketingComboRequest(
        String name,
        String description,
        UUID imageMediaId,
        String imageUrl,
        String discountType,
        Double discountValue,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active,
        List<Item> items
) {
    public record Item(
            String productId,
            String variantId,
            Integer quantity,
            String productName,
            String sku,
            String categoryName,
            String image,
            Double price
    ) {
    }
}
