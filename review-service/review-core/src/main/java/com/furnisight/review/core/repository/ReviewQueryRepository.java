package com.furnisight.review.core.repository;


import com.furnisight.review.core.dto.ReviewResponse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface ReviewQueryRepository {
    List<ReviewResponse> findByProductId(UUID productId, Integer page, Integer size);
}
