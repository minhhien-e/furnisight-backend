package com.furnisight.review.core.exception;

import com.furnisight.review.core.exception.enums.ErrorCode;
import java.util.Collections;
import java.util.Map;

public class ReviewDomainException extends BaseException {
    public ReviewDomainException(ErrorCode errorCode) {
        super(errorCode, errorCode.getMessage(), Collections.emptyMap());
    }
    public ReviewDomainException(ErrorCode errorCode, Map<String, Object> attributes) {
        super(errorCode, errorCode.getMessage(), attributes);
    }
}
