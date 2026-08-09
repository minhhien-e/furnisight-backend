package com.furnisight.cart.domain.exceptions;

public class AlreadyExistsException extends DomainException {
    public AlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
