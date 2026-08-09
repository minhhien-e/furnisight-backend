package com.furnisight.admin.stats.web.dto.response;

public record UserMetricsResponse(
        long total,
        long active,
        long banned,
        long newThisMonth
) {
}
