package com.furnisight.admin.stats.web.dto.response;

import java.util.List;

public record StatsResponse(
        UserMetricsResponse user,
        OrderMetricsResponse orders,
        ProductMetricsResponse products,
        List<CategoryMetricResponse> topCategories,
        ReviewSentimentMetricResponse reviews
) {
}
