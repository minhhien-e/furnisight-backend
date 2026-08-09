package com.furnisight.catalog.domain.exceptions;

import java.util.Map;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public InvalidOperationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public InvalidOperationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
    
    public InvalidOperationException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
