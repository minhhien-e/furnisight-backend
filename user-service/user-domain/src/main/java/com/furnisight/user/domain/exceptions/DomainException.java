package com.furnisight.user.domain.exceptions;

import com.furnisight.user.domain.exceptions.identity.ErrorCode;

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
