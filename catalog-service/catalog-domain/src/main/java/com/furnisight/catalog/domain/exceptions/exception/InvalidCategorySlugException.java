package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;

public class InvalidCategorySlugException extends BaseException {
    public InvalidCategorySlugException(String message) {
        super(ErrorCode.INVALID_CATEGORY_SLUG, message, Map.of());
    }
}
