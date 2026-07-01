package com.furnisight.cart.domain.exceptions;

public class NotFoundException extends DomainException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
