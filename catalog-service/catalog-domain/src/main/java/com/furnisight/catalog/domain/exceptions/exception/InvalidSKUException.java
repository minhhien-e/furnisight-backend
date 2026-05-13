package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class InvalidSKUException extends BaseException {
    public InvalidSKUException(String message) {
        super(ErrorCode.INVALID_SKU, message, Collections.emptyMap());
    }
}
