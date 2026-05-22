package com.furnisight.order.domain.exceptions.order;

import com.furnisight.order.domain.exceptions.DomainException;

public class NotFoundException extends DomainException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
