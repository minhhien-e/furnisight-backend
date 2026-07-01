package com.furnisight.message.domain.exceptions;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
