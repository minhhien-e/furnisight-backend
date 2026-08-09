package com.furnisight.catalog.domain.exceptions;

import java.util.Map;

public class AlreadyExistsException extends DomainException {
    public AlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AlreadyExistsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AlreadyExistsException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
    
    public AlreadyExistsException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
