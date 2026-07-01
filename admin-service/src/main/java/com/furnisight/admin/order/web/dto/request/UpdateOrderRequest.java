package com.furnisight.admin.order.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderRequest(@NotBlank(message = "Status is required") String status, String statusLabel, String trackingCode, String note) {
}
