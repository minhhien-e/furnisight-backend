package com.furnisight.review.application.review.service;

import com.furnisight.review.application.review.port.in.usecase.UpdateReviewUseCase;
import com.furnisight.review.application.review.port.out.ReviewEventPublisherPort;
import com.furnisight.review.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.review.domain.entities.Review;
import com.furnisight.review.domain.exceptions.ErrorCode;
import com.furnisight.review.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateReviewService implements UpdateReviewUseCase {

    private final ReviewWritePort reviewWritePort;
    private final ApplicationEventPublisher eventPublisher;
    private final ReviewEventPublisherPort reviewEventPublisherPort;

    @Override
    @Transactional
    public void updateReview(UUID reviewId, String title, String content, Integer rating) {
        Review review = reviewWritePort.findById(reviewId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        boolean isChanged = review.updateFromUser(title, content, rating);

        if (isChanged) {
            reviewWritePort.save(review);
            eventPublisher.publishEvent(new ReviewSentimentRequestedEvent(review.getId(), review.getContent().text()));
            reviewEventPublisherPort.publishReviewChangedEvent(review.getProductId());
        }
    }
}

