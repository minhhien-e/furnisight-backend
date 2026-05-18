package com.furnisight.review.core.model.valueobject;

import com.furnisight.review.core.exception.ReviewDomainException;
import com.furnisight.review.core.exception.enums.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Embeddable
public record ReviewVoteId(
    @Column(name = "user_id", nullable = false) UUID userId,
    @Column(name = "review_id", nullable = false) UUID reviewId
) implements Serializable {

    public ReviewVoteId {
        if (userId == null || reviewId == null) {
            throw new ReviewDomainException(ErrorCode.INVALID_VOTE_DATA,
                Map.of("reason", "IDs cannot be null"));
        }
    }
}
