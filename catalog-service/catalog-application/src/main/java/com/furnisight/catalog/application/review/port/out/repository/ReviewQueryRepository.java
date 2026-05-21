package com.furnisight.catalog.application.review.port.out.repository;

import com.furnisight.catalog.application.review.dto.ReviewProjection;
import java.util.List;
import java.util.UUID;

public interface ReviewQueryRepository {
    List<ReviewProjection> findByProductId(UUID productId, Integer page, Integer size);
}
