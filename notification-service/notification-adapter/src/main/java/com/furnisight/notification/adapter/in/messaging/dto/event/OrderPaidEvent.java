package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.util.UUID;

public record OrderPaidEvent(
    String orderCode,
    UUID userId,
    String customerEmail,
    Double paidAmount,
    String paymentMethod
) {
}
