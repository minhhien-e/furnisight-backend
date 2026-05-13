package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class DuplicateCategoryNameException extends BaseException {
    public DuplicateCategoryNameException(String name, UUID parantId) {
        super(ErrorCode.DUPLICATE_CATEGORY_NAME,
            "Category name '" + name + "' already exist under the same prarent",
            Map.of("name", name, "parantId", String.valueOf(parantId)));
    }
}
