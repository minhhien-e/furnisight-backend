package com.furnisight.review.infrastructure.elasticsearch.mapper;

import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.infrastructure.elasticsearch.document.ReviewDocument;

import java.util.UUID;

/**
 * Mapper thực hiện chuyển đổi qua lại giữa ReviewDocument (Elasticsearch),
 * Review (JPA Entity), và ReviewResponse (Application DTO).
 */
public final class ReviewEsMapper {

    private ReviewEsMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Chuyển đổi từ ReviewDocument (ES Document) sang ReviewResponse (Application DTO)
     */
    public static ReviewResponse toResponse(ReviewDocument doc) {
        if (doc == null) {
            return null;
        }
        return new ReviewResponse(
                UUID.fromString(doc.getId()),
                UUID.fromString(doc.getUserId()),
                UUID.fromString(doc.getProductId()),
                doc.getTitle(),
                doc.getContent(),
                doc.getRating(),
                doc.getStatus(),
                doc.getCreatedAt()
        );
    }

    /**
     * Chuyển đổi từ Review (Domain Entity) sang ReviewDocument (ES Document)
     */
    public static ReviewDocument toDocument(Review review) {
        if (review == null) {
            return null;
        }
        return ReviewDocument.builder()
                .id(review.getId().toString())
                .userId(review.getUserId().toString())
                .productId(review.getProductId().toString())
                .orderItemId(review.getOrderItemId().toString())
                .title(review.getTitle().value())
                .content(review.getContent().text())
                .rating(review.getRating().value())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
