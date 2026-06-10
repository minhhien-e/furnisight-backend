package com.furnisight.admin.stats.web.dto.response;

public record CategoryMetricResponse(
        String id,
        String name,
        String slug,
        long productCount
) {
}
