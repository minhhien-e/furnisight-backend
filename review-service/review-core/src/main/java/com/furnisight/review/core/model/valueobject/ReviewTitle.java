package com.furnisight.review.core.model.valueobject;

import com.furnisight.review.core.exception.ReviewDomainException;
import com.furnisight.review.core.exception.enums.ErrorCode;
import jakarta.persistence.Embeddable;
import java.util.Map;

@Embeddable
public record ReviewTitle(String value) {
    private static final int MAX_LENGTH = 255;

    public ReviewTitle {
        if (value == null || value.isBlank()) {
            value = "Product Review";
        }
        if (value.length() > MAX_LENGTH) {
            throw new ReviewDomainException(ErrorCode.INVALID_TITLE_LENGTH,
                Map.of("inputLength", value.length(), "maxLength", MAX_LENGTH));
        }
    }
}
