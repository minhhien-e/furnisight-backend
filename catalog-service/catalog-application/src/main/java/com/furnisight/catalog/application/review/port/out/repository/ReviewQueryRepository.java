package com.furnisight.catalog.application.review.port.out.repository;

import com.furnisight.catalog.application.review.dto.response.ReviewResponse;
import java.util.List;
import java.util.UUID;

public interface ReviewQueryRepository {
    List<ReviewResponse> findByProductId(UUID productId, Integer page, Integer size);
    List<ReviewResponse> findTopRandomReviews(int limit);
}
