package com.furnisight.cart.domain.exceptions;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
