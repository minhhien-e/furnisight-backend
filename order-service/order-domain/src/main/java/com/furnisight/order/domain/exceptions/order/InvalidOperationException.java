package com.furnisight.order.domain.exceptions.order;

import com.furnisight.order.domain.exceptions.DomainException;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
