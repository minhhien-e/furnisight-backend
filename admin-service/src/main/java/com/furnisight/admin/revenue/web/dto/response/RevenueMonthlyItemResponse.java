package com.furnisight.admin.revenue.web.dto.response;

public record RevenueMonthlyItemResponse(
        String month,
        long orders,
        String revenue,
        String mom,
        String momClass,
        String profit,
        String refund
) {}
