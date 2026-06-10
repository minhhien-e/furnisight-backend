package com.furnisight.admin.stats.web.dto.response;

public record OrderMetricsResponse(
        long total,
        long today,
        double totalRevenue,
        double revenueThisMonth
) {
}
