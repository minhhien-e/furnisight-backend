package com.furnisight.user.domain.exceptions.identity;

import com.furnisight.user.domain.exceptions.DomainException;

public class NotFoundException extends DomainException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
