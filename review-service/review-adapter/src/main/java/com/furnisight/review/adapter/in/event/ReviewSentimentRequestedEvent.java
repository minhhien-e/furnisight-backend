package com.furnisight.review.adapter.in.event;

import java.util.UUID;

public record ReviewSentimentRequestedEvent(UUID reviewId, String text) {
}
