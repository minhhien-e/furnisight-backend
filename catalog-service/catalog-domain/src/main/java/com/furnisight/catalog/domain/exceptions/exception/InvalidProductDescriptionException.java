package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class InvalidProductDescriptionException extends BaseException {
    public InvalidProductDescriptionException(String message) {
        super(ErrorCode.INVALID_PRODUCT_DESCRIPTION, message, Collections.emptyMap());
    }
}
