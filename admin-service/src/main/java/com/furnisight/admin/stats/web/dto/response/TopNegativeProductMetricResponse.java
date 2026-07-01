package com.furnisight.admin.stats.web.dto.response;

public record TopNegativeProductMetricResponse(
        String productId,
        String productName,
        long negativeCount,
        double negativeRatio,
        long visibleReviewCount,
        double averageRating
) {
}
