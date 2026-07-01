package com.furnisight.review.application.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewSentimentEventHandler {

    private final ReviewSentimentProcessor reviewSentimentProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewSentimentRequestedEvent event) {
        reviewSentimentProcessor.analyzeAsync(event.reviewId(), event.text());
    }
}
