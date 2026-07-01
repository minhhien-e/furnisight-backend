package com.furnisight.review.application.review.port.in.usecase;

import com.furnisight.review.application.review.dto.response.ReviewResponse;
import java.util.List;
import java.util.UUID;

public interface GetReviewsByProductUseCase {
    List<ReviewResponse> getReviewsByProduct(UUID productId, Integer page, Integer size);
}

