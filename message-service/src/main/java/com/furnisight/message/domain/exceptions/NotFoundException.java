package com.furnisight.message.domain.exceptions;

public class NotFoundException extends DomainException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
