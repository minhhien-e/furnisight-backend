package com.furnisight.review.infrastructure.database.repository.impl;

import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.furnisight.review.infrastructure.repository.jooq.tables.Reviews.REVIEWS;

@Repository("reviewQueryRepositoryImpl")
@Primary
@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final DSLContext dsl;

    @Override
    public List<ReviewResponse> findByProductId(UUID productId, Integer page, Integer size) {
        int limit = (size != null) ? size : 10;
        int offset = (page != null) ? page * limit : 0;

        return dsl.select(
                REVIEWS.ID,
                REVIEWS.USER_ID.as("userId"),
                REVIEWS.PRODUCT_ID.as("productId"),
                REVIEWS.TITLE,
                REVIEWS.CONTENT_TEXT.as("content"),
                REVIEWS.RATING,
                REVIEWS.STATUS.cast(String.class).as("status"),
                REVIEWS.CREATED_AT.as("createdAt")
            )
            .from(REVIEWS)
            .where(REVIEWS.PRODUCT_ID.eq(productId))
            .and(REVIEWS.STATUS.cast(String.class).in("PENDING", "VISIBLE"))
            .orderBy(REVIEWS.CREATED_AT.desc())
            .limit(limit)
            .offset(offset)
            .fetchInto(ReviewResponse.class);
    }
}
