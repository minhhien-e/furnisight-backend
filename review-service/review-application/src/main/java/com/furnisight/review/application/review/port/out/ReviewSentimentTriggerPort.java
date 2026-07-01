package com.furnisight.review.application.review.port.out;

import java.util.UUID;

public interface ReviewSentimentTriggerPort {
    void triggerSentimentAnalysis(UUID reviewId, String text);
}
