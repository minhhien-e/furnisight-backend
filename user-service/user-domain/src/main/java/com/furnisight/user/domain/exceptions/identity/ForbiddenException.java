package com.furnisight.user.domain.exceptions.identity;

import com.furnisight.user.domain.exceptions.DomainException;

public class ForbiddenException extends DomainException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
