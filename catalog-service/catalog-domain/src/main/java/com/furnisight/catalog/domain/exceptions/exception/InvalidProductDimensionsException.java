package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class InvalidProductDimensionsException extends BaseException {
    public InvalidProductDimensionsException(String message) {
        super(ErrorCode.INVALID_PRODUCT_DIMENSIONS, message, Collections.emptyMap());
    }
}
