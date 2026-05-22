package com.furnisight.order.domain.exceptions;

import com.furnisight.order.domain.exceptions.order.ErrorCode;

public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
