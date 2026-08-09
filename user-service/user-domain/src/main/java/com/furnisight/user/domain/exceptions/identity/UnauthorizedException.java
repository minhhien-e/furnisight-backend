package com.furnisight.user.domain.exceptions.identity;

import com.furnisight.user.domain.exceptions.DomainException;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
