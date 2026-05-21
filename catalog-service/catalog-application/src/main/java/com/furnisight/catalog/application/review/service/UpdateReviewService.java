package com.furnisight.catalog.application.review.service;

import com.furnisight.catalog.application.review.port.in.usecase.UpdateReviewUseCase;
import com.furnisight.catalog.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.catalog.domain.entities.Review;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateReviewService implements UpdateReviewUseCase {

    private final ReviewWritePort reviewWritePort;

    @Override
    @Transactional
    public void updateReview(UUID reviewId, String title, String content, Integer rating) {
        Review review = reviewWritePort.findById(reviewId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        boolean isChanged = review.updateFromUser(title, content, rating);

        if (isChanged) {
            reviewWritePort.save(review);
        }
    }
}
