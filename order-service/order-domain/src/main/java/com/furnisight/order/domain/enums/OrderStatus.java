package com.furnisight.order.domain.enums;

public enum OrderStatus {
    UNPAID,
    CONFIRMED,
    PAID,
    IN_TRANSIT,
    SHIPPING,
    DELIVERED,
    CANCELLED,
    CANCELLED_BY_ADMIN,
    REFUND_PENDING,
    REFUNDED,
    PAYMENT_FAILED
}
