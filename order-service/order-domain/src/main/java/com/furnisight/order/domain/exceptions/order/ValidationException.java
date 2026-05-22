package com.furnisight.order.domain.exceptions.order;

import com.furnisight.order.domain.exceptions.DomainException;

public class ValidationException extends DomainException {
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
