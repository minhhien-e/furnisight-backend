package com.furnisight.admin.controller.dto;

public record AdminStatsOrderMetricsResponse(
        long total,
        long today,
        double totalRevenue,
        double revenueThisMonth
) {
}
