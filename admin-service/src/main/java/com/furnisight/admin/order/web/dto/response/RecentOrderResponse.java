package com.furnisight.admin.order.web.dto.response;

public record RecentOrderResponse(
        String id,
        String customer,
        String total,
        String status,
        String statusLabel
) {
}
