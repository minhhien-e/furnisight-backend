package com.furnisight.review.domain.valueobjects.review;

import com.furnisight.review.domain.exceptions.ErrorCode;
import com.furnisight.review.domain.exceptions.ValidationException;
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
            throw new ValidationException(ErrorCode.INVALID_TITLE_LENGTH,
                Map.of("inputLength", value.length(), "maxLength", MAX_LENGTH));
        }
    }
}

