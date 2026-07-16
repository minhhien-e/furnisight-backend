package com.furnisight.review.adapter.in.event;

import com.furnisight.review.application.review.port.out.ReviewSentimentPort;
import com.furnisight.review.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.review.domain.entities.Review;
import com.furnisight.review.domain.exceptions.ErrorCode;
import com.furnisight.review.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSentimentProcessor {

    private final ReviewWritePort reviewWritePort;
    private final ReviewSentimentPort reviewSentimentPort;

    @Async
    @Transactional
    public void analyzeAsync(UUID reviewId, String text) {
        Review review = reviewWritePort.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        try {
            ReviewSentimentPort.SentimentResult result = reviewSentimentPort.analyze(text);
            review.markSentimentCompleted(result.sentiment(), BigDecimal.valueOf(result.confidence()));
        } catch (Exception ex) {
            log.warn("Review sentiment analysis failed for reviewId={}", reviewId, ex);
            review.markSentimentFailed(ex.getMessage());
        }

        reviewWritePort.save(review);
    }
}
