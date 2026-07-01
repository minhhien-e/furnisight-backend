package com.furnisight.review.domain.exceptions;

import java.util.Map;

public class NotFoundException extends DomainException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, Map<String, Object> attributes) {
        super(errorCode, attributes);
    }
}

