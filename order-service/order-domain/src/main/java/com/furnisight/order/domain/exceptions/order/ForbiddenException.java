package com.furnisight.order.domain.exceptions.order;

import com.furnisight.order.domain.exceptions.DomainException;

public class ForbiddenException extends DomainException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
