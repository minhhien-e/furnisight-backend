package com.furnisight.catalog.application.review.port.in.usecase;

import com.furnisight.catalog.application.review.dto.ReviewProjection;
import java.util.List;
import java.util.UUID;

public interface GetReviewsByProductUseCase {
    List<ReviewProjection> getReviewsByProduct(UUID productId, Integer page, Integer size);
}
