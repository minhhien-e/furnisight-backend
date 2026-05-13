package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;

public class InvalidCategoryNameException extends BaseException {
    public InvalidCategoryNameException(String message) {
        super(ErrorCode.INVALID_CATEGORY_NAME, message, Map.of());
    }
}
