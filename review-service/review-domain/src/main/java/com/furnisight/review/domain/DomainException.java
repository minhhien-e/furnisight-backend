package com.furnisight.review.domain.exceptions;

import lombok.Getter;
import java.util.Map;
import java.util.Collections;

@Getter
public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> attributes;

    protected DomainException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.attributes = Collections.emptyMap();
    }

    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.attributes = Collections.emptyMap();
    }
    
    protected DomainException(ErrorCode errorCode, Map<String, Object> attributes) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.attributes = attributes;
    }
}

