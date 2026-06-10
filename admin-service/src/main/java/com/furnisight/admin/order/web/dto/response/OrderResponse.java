package com.furnisight.admin.order.web.dto.response;

public record OrderResponse(
        String id,
        String customer,
        int items,
        double total,
        String status,
        String statusLabel,
        String date
) {
}
