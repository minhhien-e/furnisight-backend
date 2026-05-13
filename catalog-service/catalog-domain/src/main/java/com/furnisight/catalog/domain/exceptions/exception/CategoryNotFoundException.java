package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class CategoryNotFoundException extends BaseException {
    public CategoryNotFoundException(UUID id) {
      super(ErrorCode.CATEGORY_NOT_FOUND, "Category with id: " + id + " not found", Map.of("id", id));
    }
}
