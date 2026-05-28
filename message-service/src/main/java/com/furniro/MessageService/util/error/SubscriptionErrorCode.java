package com.furniro.MessageService.util.error;

import lombok.Getter;

@Getter
public enum SubscriptionErrorCode {
    SUBSCRIPTION_ALREADY_EXISTS(400, "Subscription already exists"),
    SUBSCRIPTION_NOT_FOUND(404, "Subscription not found"),
    SUBSCRIPTION_FAILED(500, "Subscription failed");

    private final int code;
    private final String message;

    SubscriptionErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
