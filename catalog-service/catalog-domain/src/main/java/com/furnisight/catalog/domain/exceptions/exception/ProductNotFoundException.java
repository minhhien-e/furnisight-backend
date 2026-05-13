package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException(UUID id) {
        super(ErrorCode.PRODUCT_NOT_FOUND, "Product with Id: " + id + " not found", Map.of("id", id));
    }
}
