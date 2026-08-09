package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
    String orderCode,
    UUID userId,
    String customerEmail,
    Double totalAmount,
    String createdAt,
    List<OrderItemDto> items
) {
    public record OrderItemDto(
        String productName,
        Integer quantity,
        Double price
    ) {}
}
