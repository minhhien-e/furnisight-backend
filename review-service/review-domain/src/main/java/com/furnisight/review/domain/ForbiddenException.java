package com.furnisight.review.domain.exceptions;

public class ForbiddenException extends DomainException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}

