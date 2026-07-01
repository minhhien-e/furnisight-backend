package com.furnisight.review.adapter.out.event;

import com.furnisight.review.application.review.port.out.ReviewSentimentTriggerPort;
import com.furnisight.review.adapter.in.event.ReviewSentimentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpringEventReviewSentimentTriggerAdapter implements ReviewSentimentTriggerPort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void triggerSentimentAnalysis(UUID reviewId, String text) {
        eventPublisher.publishEvent(new ReviewSentimentRequestedEvent(reviewId, text));
    }
}
