package com.furnisight.order.domain.exceptions.order;

import com.furnisight.order.domain.exceptions.DomainException;

public class AlreadyExistsException extends DomainException {
    public AlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
