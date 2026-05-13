package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;

public class DuplicatedProductNameException extends BaseException {
    public DuplicatedProductNameException(String message) {
        super(ErrorCode.INVALID_PRODUCT_NAME, message, Map.of());
    }
}
