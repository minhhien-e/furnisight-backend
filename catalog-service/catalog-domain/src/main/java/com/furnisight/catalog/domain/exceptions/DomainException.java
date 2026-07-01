package com.furnisight.catalog.domain.exceptions;

import lombok.Getter;
import java.util.Map;

@Getter
public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.details = new java.util.HashMap<>();
    }
    
    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = new java.util.HashMap<>();
    }

    public DomainException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public DomainException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
}
