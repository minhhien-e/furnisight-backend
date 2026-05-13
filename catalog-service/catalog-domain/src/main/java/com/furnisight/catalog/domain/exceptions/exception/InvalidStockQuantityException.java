package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class InvalidStockQuantityException extends BaseException {
    public InvalidStockQuantityException(String message) {
        super(ErrorCode.INVALID_STOCK_QUANTITY, message, Collections.emptyMap());
    }
}
