package com.furnisight.review.application.review.dto.response;

import java.util.UUID;

public record ProductReviewStatResponse(
        UUID productId,
        double averageRating,
        int ratingCount,
        int visibleReviewCount
) {
}
