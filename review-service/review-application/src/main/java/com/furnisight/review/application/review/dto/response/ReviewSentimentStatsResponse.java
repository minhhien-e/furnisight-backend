package com.furnisight.review.application.review.dto.response;

import java.util.List;
import java.util.UUID;

public record ReviewSentimentStatsResponse(
        long totalReviews,
        long analyzedReviews,
        long pendingReviews,
        long failedReviews,
        long positiveCount,
        long neutralCount,
        long negativeCount,
        List<TopNegativeProductResponse> topNegativeProducts
) {

    public record TopNegativeProductResponse(
            UUID productId,
            String productName,
            long negativeCount,
            double negativeRatio,
            long visibleReviewCount,
            double averageRating
    ) {
    }
}
