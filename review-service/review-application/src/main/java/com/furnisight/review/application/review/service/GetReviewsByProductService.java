package com.furnisight.review.application.review.service;

import com.furnisight.review.application.review.dto.response.ReviewResponse;
import com.furnisight.review.application.review.port.in.usecase.GetReviewsByProductUseCase;
import com.furnisight.review.application.review.port.out.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetReviewsByProductService implements GetReviewsByProductUseCase {

    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(UUID productId, Integer page, Integer size) {
        return reviewQueryRepository.findByProductId(productId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(UUID productId, Integer page, Integer size, String sentiment) {
        return reviewQueryRepository.findByProductIdAndSentiment(productId, sentiment, page, size);
    }
}
