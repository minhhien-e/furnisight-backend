package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusChangedEvent(
    UUID userId,
    String customerEmail,
    String orderCode,
    String previousStatus,
    String nextStatus,
    String paymentMethod,
    LocalDateTime occurredAt
) {
}
