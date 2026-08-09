package com.furnisight.message.domain.exceptions;

public class ValidationException extends DomainException {
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
