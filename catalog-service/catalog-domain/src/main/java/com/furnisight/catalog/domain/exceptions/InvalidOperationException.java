package com.furnisight.catalog.domain.exceptions;

import java.util.Map;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidOperationException(ErrorCode errorCode, Map<String, Object> attributes) {
        super(errorCode, attributes);
    }
}
