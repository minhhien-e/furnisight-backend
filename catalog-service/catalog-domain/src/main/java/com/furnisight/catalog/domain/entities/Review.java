package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.enums.ReviewStatus;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.InvalidOperationException;
import com.furnisight.catalog.domain.exceptions.ValidationException;
import com.furnisight.catalog.domain.services.review.ProfanityPolicy;

import com.furnisight.catalog.domain.valueobjects.review.ReviewContent;
import com.furnisight.catalog.domain.valueobjects.review.ReviewTitle;
import com.furnisight.catalog.domain.valueobjects.review.StarRating;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "title", nullable = false))
    private ReviewTitle title;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID orderItemId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "text", column = @Column(name = "content_text", nullable = false)),
        @AttributeOverride(name = "hash", column = @Column(name = "content_hash", nullable = false))
    })
    private ReviewContent content;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "rating", nullable = false))
    private StarRating rating;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "review_status")
    private ReviewStatus status;

    @Column(precision = 3, scale = 2)
    private BigDecimal trustScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Review(ReviewTitle title, UUID userId, UUID productId, UUID orderItemId,
                  ReviewContent content, StarRating rating) {

        if(userId== null ){
            throw new ValidationException(ErrorCode.USER_ID_MISSING);
        }
        if(productId== null){
            throw new ValidationException(ErrorCode.PRODUCT_ID_MISSING);
        }
        if(orderItemId== null){
            throw new ValidationException(ErrorCode.ORDER_ITEM_ID_MISSING);
        }
        if(title== null || title.value().isBlank()){
            throw new ValidationException(ErrorCode.TITLE_MISSING);
        }
        if(rating== null || rating.value()<1 || rating.value()>5){
            throw new ValidationException(ErrorCode.RATING_VALUE_MISSING);
        }

        if(content== null || content.text().isBlank() || content.hash().isBlank()){
            throw new ValidationException(ErrorCode.CONTENT_MISSING);
        }

        this.id = UUID.randomUUID();
        this.title = title;
        this.userId = userId;
        this.productId = productId;
        this.orderItemId = orderItemId;
        this.content = content;
        this.rating = rating;

        this.status = ReviewStatus.PENDING;
        this.trustScore = BigDecimal.valueOf(0.50);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean applyModeration(ProfanityPolicy profanityPolicy) {
        boolean hasProfanity = profanityPolicy.containsForbiddenWords(this.content.text());

        if (hasProfanity) {
            hide();
        }

        return hasProfanity;
    }
    public void approve() {
        ensureNotArchived();
        if (this.status != ReviewStatus.PENDING) {
            throw new InvalidOperationException(ErrorCode.INVALID_REVIEW_STATE,
                Map.of("currentStatus", this.status, "expectedStatus", ReviewStatus.PENDING));
        }
        this.status = ReviewStatus.VISIBLE;
        touch();
    }

    public void hide() {
        ensureNotArchived();
        this.status = ReviewStatus.HIDDEN;
        touch();
    }

    public void applyShadowBan() {
        ensureNotArchived();
        this.status = ReviewStatus.SHADOW_BANNED;
        touch();
    }

    public void archive() {
        this.status = ReviewStatus.ARCHIVED;
        touch();
    }

    public void updateTrustScore(BigDecimal newScore) {
        if (this.status == ReviewStatus.ARCHIVED) {
            throw new InvalidOperationException(ErrorCode.REVIEW_ALREADY_ARCHIVED);
        }
        this.trustScore = newScore;
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    private void ensureNotArchived() {
        if (this.status == ReviewStatus.ARCHIVED) {
            throw new InvalidOperationException(ErrorCode.REVIEW_ALREADY_ARCHIVED);
        }
    }


    public boolean isHidden() {
        return this.status.equals(ReviewStatus.HIDDEN);
    }

    public boolean updateFromUser(String newTitle, String newContent, Integer newRating) {
       if (!"VISIBLE".equalsIgnoreCase(this.status.name())) {
            throw new InvalidOperationException(ErrorCode.REVIEW_IN_PROCESS);
        }
        boolean isChanged = false;
        if (newTitle != null && !newTitle.equals(this.title.value())) {
            this.title = new ReviewTitle(newTitle);
            isChanged = true;
        }

        if (newContent != null && !newContent.equals(this.content.text())) {
            this.content = ReviewContent.from(newContent);
            isChanged = true;
        }

        if (newRating != null && (this.rating == null || !newRating.equals(this.rating.value()))) {
            if (newRating < 1 || newRating > 5) {
                throw new ValidationException(ErrorCode.INVALID_RATING);
            }
            this.rating = new StarRating(newRating);
            isChanged = true;
        }

        if (isChanged) {
            this.updatedAt = LocalDateTime.now();
            this.status  = ReviewStatus.PENDING ;
        }

        return isChanged;
    }
}
