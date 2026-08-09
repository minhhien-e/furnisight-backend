package com.furnisight.notification.domain.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INBOX_MESSAGE_NOT_FOUND("Inbox message not found"),
    NOTIFICATION_LOG_NOT_FOUND("Notification log not found"),
    NOTIFICATION_PROFILE_NOT_ALLOWED("Notification profile not allowed"),
    NOTIFICATION_PROFILE_NOT_FOUND("Notification profile not found"),
    NOTIFICATION_TEMPLATE_NOT_FOUND("Notification template not found"),
    UNSUPPORTED_NOTIFICATION_CHANNEL("Unsupported notification channel"),
    ORDER_NOT_FOUND("Order not found"),
    INVALID_ORDER_STATUS("Invalid order status"),
    ORDER_ITEM_EMPTY("Order must contain at least one item"),
    PRODUCT_ID_EMPTY("Product ID cannot be empty"),
    NEGATIVE_PRICE("Price cannot be negative"),
    INVALID_QUANTITY("Quantity must be greater than zero"),
    INVALID_FEE("Fee cannot be negative"),
    INVALID_DISCOUNT("Discount cannot be negative"),
    INVALID_PAYMENT_AMOUNT("Payment amount cannot be negative"),
    INVALID_PAYMENT_METHOD("Payment method cannot be empty"),
    INVALID_SHIPPING_INFO("Shipping information cannot be empty"),
    INVALID_PRODUCT_INFO("Product information cannot be empty"),
    INSUFFICIENT_STOCK("Insufficient stock for requested product quantity"),
    INVALID_PRODUCT_DIMENSIONS("Product dimensions must be greater than zero"),
    PAYMENT_FAILED("Payment failed"),
    INVALID_SIGNATURE("Invalid signature"),
    UNAUTHORIZED("User not authenticated");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }
}
