package com.furnisight.admin.marketing.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MarketingComboResponse(
        String id,
        String name,
        String description,
        UUID imageMediaId,
        String imageUrl,
        String discountType,
        Double discountValue,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active,
        List<String> placements,
        List<Item> items,
        Integer itemCount,
        Double originalAmount,
        Double finalAmount,
        Double savedAmount,
        Long usedCount,
        String status,
        LocalDateTime createdAt
) {
    public record Item(
            String productId,
            String variantId,
            String productName,
            String sku,
            String categoryName,
            String image,
            Double price,
            Integer quantity,
            Boolean snapshotMissing
    ) {
    }
}
