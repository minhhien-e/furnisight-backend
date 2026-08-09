package com.furnisight.admin.review.infrastructure.grpc;

import com.furnisight.admin.review.AdminReviewServiceGrpc;
import com.furnisight.admin.review.ReviewSentimentStatsResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class AdminReviewGrpcClient {

    @GrpcClient("reviewService")
    private AdminReviewServiceGrpc.AdminReviewServiceBlockingStub adminReviewServiceStub;

    public ReviewSentimentStatsResponse getReviewSentimentStats() {
        return adminReviewServiceStub.getReviewSentimentStats(com.google.protobuf.Empty.getDefaultInstance());
    }
}
