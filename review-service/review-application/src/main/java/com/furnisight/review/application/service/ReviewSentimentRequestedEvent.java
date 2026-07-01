package com.furnisight.review.application.review.service;

import java.util.UUID;

public record ReviewSentimentRequestedEvent(UUID reviewId, String text) {
}
