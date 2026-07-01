package com.furnisight.review.domain.exceptions;

import java.util.Map;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidOperationException(ErrorCode errorCode, Map<String, Object> attributes) {
        super(errorCode, attributes);
    }
}

