package com.furnisight.admin.stats.web.dto.response;

import java.util.List;

public record ReviewSentimentMetricResponse(
        long totalReviews,
        long analyzedReviews,
        long pendingReviews,
        long failedReviews,
        long positiveCount,
        long neutralCount,
        long negativeCount,
        List<TopNegativeProductMetricResponse> topNegativeProducts
) {
}
