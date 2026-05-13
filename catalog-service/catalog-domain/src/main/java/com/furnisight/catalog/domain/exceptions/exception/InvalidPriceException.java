package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class InvalidPriceException extends BaseException {
    public InvalidPriceException(String message) {
        super(ErrorCode.INVALID_PRICE, message, Collections.emptyMap());
    }
}
