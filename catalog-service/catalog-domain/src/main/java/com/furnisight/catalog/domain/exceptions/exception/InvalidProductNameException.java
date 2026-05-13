package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class InvalidProductNameException extends BaseException {
    public InvalidProductNameException(String message) {
        super(ErrorCode.INVALID_PRODUCT_NAME, message, Collections.emptyMap());
    }
}
