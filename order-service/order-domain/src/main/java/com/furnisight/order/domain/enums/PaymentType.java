package com.furnisight.order.domain.enums;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;

import java.util.Arrays;

public enum PaymentType {
    COD,
    VNPAY;

    public static PaymentType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD));
    }
}
