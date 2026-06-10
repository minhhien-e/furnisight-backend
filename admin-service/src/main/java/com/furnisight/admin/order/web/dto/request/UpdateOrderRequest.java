package com.furnisight.admin.order.web.dto.request;

public record UpdateOrderRequest(String status, String statusLabel, String trackingCode, String note) {
}
