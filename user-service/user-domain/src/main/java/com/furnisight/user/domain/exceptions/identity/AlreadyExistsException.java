package com.furnisight.user.domain.exceptions.identity;

import com.furnisight.user.domain.exceptions.DomainException;

public class AlreadyExistsException extends DomainException {
    public AlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
