package com.furnisight.admin.order.web.dto.response;

public record OrderResponse(
        String id,
        String orderCode,
        String status,
        double totalAmount,
        String createdAt,
        String paymentMethod,
        String firstProductImage,
        String customer,
        int itemCount,
        String trackingCode
) {
}
