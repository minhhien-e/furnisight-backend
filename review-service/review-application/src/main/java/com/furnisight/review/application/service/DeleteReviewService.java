package com.furnisight.review.application.review.service;

import com.furnisight.review.application.review.port.in.usecase.DeleteReviewUseCase;
import com.furnisight.review.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.review.domain.entities.Review;
import com.furnisight.review.domain.exceptions.ErrorCode;
import com.furnisight.review.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteReviewService implements DeleteReviewUseCase {

    private final ReviewWritePort reviewWritePort;

    @Override
    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewWritePort.findById(reviewId)
            .orElseThrow(() -> new NotFoundException(
                ErrorCode.REVIEW_NOT_FOUND,
                Map.of("id", reviewId)
            ));

        reviewWritePort.deleteById(review.getId());
    }
}

