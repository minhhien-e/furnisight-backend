package com.furnisight.admin.controller.dto;

public record AdminStatsProductMetricsResponse(
        long total,
        long lowStock
) {
}
