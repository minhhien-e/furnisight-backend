package com.furnisight.review.application.port.out;

public interface ReviewSentimentPort {

    SentimentResult analyze(String text);

    record SentimentResult(String sentiment, double confidence) {
    }
}
