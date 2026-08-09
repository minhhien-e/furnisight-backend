package com.furnisight.review.adapter.out.client;

import com.furnisight.ai.review.AnalyzeReviewSentimentRequest;
import com.furnisight.ai.review.ReviewSentimentServiceGrpc;
import com.furnisight.review.application.review.port.out.ReviewSentimentPort;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class ReviewSentimentGrpcClient implements ReviewSentimentPort {

    @GrpcClient("ai-review-sentiment")
    private ReviewSentimentServiceGrpc.ReviewSentimentServiceBlockingStub sentimentServiceStub;

    @Override
    public SentimentResult analyze(String text) {
        var response = sentimentServiceStub.analyzeReviewSentiment(
                AnalyzeReviewSentimentRequest.newBuilder()
                        .setText(text == null ? "" : text)
                        .build()
        );
        return new SentimentResult(response.getSentiment(), response.getConfidence());
    }
}
