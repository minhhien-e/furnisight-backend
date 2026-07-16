package com.furnisight.review.application.review.service;

import com.furnisight.review.application.review.dto.response.ReviewResponse;
import com.furnisight.review.application.review.port.in.usecase.GetTopRandomReviewsUseCase;
import com.furnisight.review.application.review.port.out.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTopRandomReviewsService implements GetTopRandomReviewsUseCase {

    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    @Cacheable(value = "top_random_reviews", key = "#p0")
    public List<ReviewResponse> getTopRandomReviews(int limit) {
        return reviewQueryRepository.findTopRandomReviews(limit);
    }
}

