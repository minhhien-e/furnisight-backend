package com.furnisight.order.domain.exceptions.order;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ORDER_NOT_FOUND("Order not found"),
    INVALID_ORDER_STATUS("Invalid order status"),
    ORDER_ITEM_EMPTY("Order must contain at least one item");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }
}
