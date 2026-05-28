package com.furnisight.order.domain.exceptions;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
