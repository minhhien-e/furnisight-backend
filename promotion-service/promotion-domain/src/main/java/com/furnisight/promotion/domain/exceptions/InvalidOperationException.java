package com.furnisight.promotion.domain.exceptions;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
