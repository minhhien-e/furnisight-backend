package com.furnisight.admin.controller.dto;

public record AdminStatsUserMetricsResponse(
        long total,
        long active,
        long banned,
        long newThisMonth
) {
}
