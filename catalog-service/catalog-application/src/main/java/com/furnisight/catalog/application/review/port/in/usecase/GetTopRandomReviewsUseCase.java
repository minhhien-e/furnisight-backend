package com.furnisight.catalog.application.review.port.in.usecase;

import com.furnisight.catalog.application.review.dto.response.ReviewResponse;
import java.util.List;

public interface GetTopRandomReviewsUseCase {
    List<ReviewResponse> getTopRandomReviews(int limit);
}
