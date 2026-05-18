package com.furnisight.review.core.model.entity;

import com.furnisight.review.core.exception.ReviewDomainException;
import com.furnisight.review.core.exception.enums.ErrorCode;
import com.furnisight.review.core.model.valueobject.ClientIp;
import com.furnisight.review.core.model.valueobject.ReviewContent;
import com.furnisight.review.core.model.valueobject.StarRating;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_edit_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEditHistory {

    @Id
    private UUID id;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "text", column = @Column(name = "old_content_text")),
        @AttributeOverride(name = "hash", column = @Column(name = "old_content_hash"))
    })
    private ReviewContent oldContent;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "text", column = @Column(name = "new_content_text")),
        @AttributeOverride(name = "hash", column = @Column(name = "new_content_hash"))
    })
    private ReviewContent newContent;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "old_rating"))
    private StarRating oldRating;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "new_rating"))
    private StarRating newRating;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "client_ip"))
    private ClientIp clientIp;

    @Column(name = "edited_at", nullable = false, updatable = false)
    private LocalDateTime editedAt;

    public ReviewEditHistory(UUID reviewId,
                             ReviewContent oldContent, ReviewContent newContent,
                             StarRating oldRating, StarRating newRating,
                             ClientIp clientIp) {
        if (reviewId == null) {
            throw new ReviewDomainException(ErrorCode.REVIEW_ID_MISSING);
        }
        if (newContent == null || newContent.text().isBlank()) {
            throw new ReviewDomainException(ErrorCode.CONTENT_MISSING);
        }
        if (newRating == null || newRating.value() < 1 || newRating.value() > 5) {
            throw new ReviewDomainException(ErrorCode.RATING_VALUE_MISSING);
        }
        if (clientIp == null) {
            throw new ReviewDomainException(ErrorCode.IP_MISSING);
        }
        this.id = UUID.randomUUID();
        this.reviewId = reviewId;
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.oldRating = oldRating;
        this.newRating = newRating;
        this.clientIp = clientIp;
        this.editedAt = LocalDateTime.now();
    }
}
