package com.furnisight.catalog.domain.valueobjects.review;

import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.ValidationException;
import jakarta.persistence.Embeddable;
import java.util.Map;

@Embeddable
public record StarRating(int value) {
    public StarRating {
        if (value < 1 || value > 5) {
            throw new ValidationException(ErrorCode.INVALID_RATING,
                Map.of("actualValue", value, "range", "1-5"));
        }
    }
}
