package com.furnisight.admin.revenue.web.dto.response;

public record RevenueMonthlyItemResponse(
        String month,
        long orders,
        double revenue,
        Double momChangePct,
        String profit,
        String refund
) {}
