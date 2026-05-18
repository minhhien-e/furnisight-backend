package com.furnisight.review.core.model.valueobject;

import com.furnisight.review.core.exception.ReviewDomainException;
import com.furnisight.review.core.exception.enums.ErrorCode;
import jakarta.persistence.Embeddable;
import java.util.Map;

@Embeddable
public record StarRating(int value) {
    public StarRating {
        if (value < 1 || value > 5) {
            throw new ReviewDomainException(ErrorCode.INVALID_RATING,
                Map.of("actualValue", value, "range", "1-5"));
        }
    }
}
