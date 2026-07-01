package com.furnisight.review.application.review.port.in.usecase;

import com.furnisight.review.application.review.dto.response.ReviewResponse;
import java.util.List;

public interface GetTopRandomReviewsUseCase {
    List<ReviewResponse> getTopRandomReviews(int limit);
}

