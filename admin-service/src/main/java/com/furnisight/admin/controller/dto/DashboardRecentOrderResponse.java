package com.furnisight.admin.controller.dto;

public record DashboardRecentOrderResponse(
        String id,
        String customer,
        String total,
        String status,
        String statusLabel
) {
}
