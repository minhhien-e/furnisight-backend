package com.furnisight.admin.controller.dto;

public record AdminOrderResponse(
        String id,
        String customer,
        int items,
        double total,
        String status,
        String statusLabel,
        String date
) {
}
