package com.furnisight.review.application.review.service;

import com.furnisight.review.application.review.dto.response.ReviewResponse;
import com.furnisight.review.application.review.port.in.usecase.GetCurrentUserReviewsByOrderItemsUseCase;
import com.furnisight.review.application.review.port.out.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentUserReviewsByOrderItemsService implements GetCurrentUserReviewsByOrderItemsUseCase {

    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getCurrentUserReviewsByOrderItemIds(UUID userId, List<UUID> orderItemIds) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            return List.of();
        }
        return reviewQueryRepository.findByUserIdAndOrderItemIds(userId, orderItemIds);
    }
}
