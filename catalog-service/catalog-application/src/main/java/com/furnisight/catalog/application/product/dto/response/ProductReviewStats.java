package com.furnisight.catalog.application.product.dto.response;

import java.util.UUID;

public record ProductReviewStats(
        UUID productId,
        double rating,
        int ratingCount
) {
}
