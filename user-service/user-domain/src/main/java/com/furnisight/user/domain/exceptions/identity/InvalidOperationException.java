package com.furnisight.user.domain.exceptions.identity;

import com.furnisight.user.domain.exceptions.DomainException;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
