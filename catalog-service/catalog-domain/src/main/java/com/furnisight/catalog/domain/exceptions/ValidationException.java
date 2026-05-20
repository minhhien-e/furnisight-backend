package com.furnisight.catalog.domain.exceptions;

import java.util.Map;

public class ValidationException extends DomainException {
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public ValidationException(ErrorCode errorCode, Map<String, Object> attributes) {
        super(errorCode, attributes);
    }
}
