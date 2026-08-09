package com.furnisight.message.domain.exceptions;

public class AlreadyExistsException extends DomainException {
    public AlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
