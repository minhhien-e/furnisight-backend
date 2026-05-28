package com.furniro.MessageService.exception.imp;

import com.furniro.MessageService.exception.BaseException;
import com.furniro.MessageService.util.error.SubscriptionErrorCode;

import lombok.Getter;

@Getter
public class SubscriptionException extends BaseException {
    private final SubscriptionErrorCode subscriptionErrorCode;

    public SubscriptionException(SubscriptionErrorCode subscriptionErrorCode) {
        super(subscriptionErrorCode.getCode(), subscriptionErrorCode.getMessage());
        this.subscriptionErrorCode = subscriptionErrorCode;
    }
}
