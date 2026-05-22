package com.furnisight.order.domain.exceptions.order;

import com.furnisight.order.domain.exceptions.DomainException;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
