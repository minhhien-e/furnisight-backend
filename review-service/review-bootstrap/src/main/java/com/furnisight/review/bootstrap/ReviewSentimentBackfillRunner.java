package com.furnisight.review.bootstrap;

import com.furnisight.review.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.review.application.review.service.ReviewSentimentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSentimentBackfillRunner implements ApplicationRunner {

    private final ReviewWritePort reviewWritePort;
    private final ReviewSentimentProcessor reviewSentimentProcessor;

    @Override
    public void run(ApplicationArguments args) {
        var reviews = new ArrayList<>(reviewWritePort.findBySentimentStatusIn(List.of("PENDING", "FAILED")));
        reviews.addAll(reviewWritePort.findHeuristicNeutralReviews());
        if (reviews.isEmpty()) {
            return;
        }

        log.info("Scheduling sentiment reprocessing for {} reviews", reviews.size());
        for (var review : reviews) {
            reviewSentimentProcessor.analyzeAsync(review.getId(), review.getContent().text());
        }
    }
}
