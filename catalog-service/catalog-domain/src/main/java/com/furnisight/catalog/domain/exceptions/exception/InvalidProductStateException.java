package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class InvalidProductStateException extends BaseException {
    public InvalidProductStateException(String message) {
        super(ErrorCode.INVALID_PRODUCT_STATE, message, Collections.emptyMap());
    }
}
