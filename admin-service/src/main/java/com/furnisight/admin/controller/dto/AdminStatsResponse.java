package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminStatsResponse(
        AdminStatsUserMetricsResponse user,
        AdminStatsOrderMetricsResponse orders,
        AdminStatsProductMetricsResponse products,
        List<AdminStatsCategoryMetricResponse> topCategories
) {
}
