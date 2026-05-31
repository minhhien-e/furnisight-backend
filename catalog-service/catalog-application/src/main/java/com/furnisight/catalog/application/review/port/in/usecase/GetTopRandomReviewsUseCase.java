package com.furnisight.catalog.application.review.port.in.usecase;

import com.furnisight.catalog.application.review.dto.ReviewProjection;
import java.util.List;

public interface GetTopRandomReviewsUseCase {
    List<ReviewProjection> getTopRandomReviews(int limit);
}
