package com.furnisight.admin.stats.web.dto.response;

public record ProductMetricsResponse(
        long total,
        long lowStock
) {
}
