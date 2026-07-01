package com.furnisight.review.adapter.in.grpc;

import com.furnisight.admin.review.AdminReviewServiceGrpc;
import com.furnisight.admin.review.ReviewSentimentStatsResponse;
import com.furnisight.admin.review.TopNegativeProduct;
import com.furnisight.review.application.review.port.out.repository.ReviewQueryRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AdminReviewGrpcService extends AdminReviewServiceGrpc.AdminReviewServiceImplBase {

    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    public void getReviewSentimentStats(com.google.protobuf.Empty request, StreamObserver<ReviewSentimentStatsResponse> responseObserver) {
        var stats = reviewQueryRepository.findReviewSentimentStats();

        responseObserver.onNext(ReviewSentimentStatsResponse.newBuilder()
                .setTotalReviews(stats.totalReviews())
                .setAnalyzedReviews(stats.analyzedReviews())
                .setPendingReviews(stats.pendingReviews())
                .setFailedReviews(stats.failedReviews())
                .setPositiveCount(stats.positiveCount())
                .setNeutralCount(stats.neutralCount())
                .setNegativeCount(stats.negativeCount())
                .addAllTopNegativeProducts(stats.topNegativeProducts().stream()
                        .map(product -> TopNegativeProduct.newBuilder()
                                .setProductId(product.productId().toString())
                                .setProductName(product.productName() == null ? "" : product.productName())
                                .setNegativeCount(product.negativeCount())
                                .setNegativeRatio(product.negativeRatio())
                                .setVisibleReviewCount(product.visibleReviewCount())
                                .setAverageRating(product.averageRating())
                                .build())
                        .toList())
                .build());
        responseObserver.onCompleted();
    }
}
