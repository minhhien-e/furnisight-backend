package com.furnisight.catalog.infrastructure.integration.grpc;

import com.furnisight.catalog.application.product.dto.response.ProductReviewStats;
import com.furnisight.catalog.application.product.port.out.ProductReviewStatsPort;
import com.furnisight.review.BatchGetProductReviewStatsRequest;
import com.furnisight.review.ReviewServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GrpcReviewStatsClient implements ProductReviewStatsPort {

    @GrpcClient("reviewService")
    private ReviewServiceGrpc.ReviewServiceBlockingStub reviewServiceStub;

    @Override
    public Map<UUID, ProductReviewStats> getProductReviewStats(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        return reviewServiceStub.batchGetProductReviewStats(
                        BatchGetProductReviewStatsRequest.newBuilder()
                                .addAllProductIds(productIds.stream().map(UUID::toString).toList())
                                .build()
                )
                .getStatsList()
                .stream()
                .map(stat -> new ProductReviewStats(
                        UUID.fromString(stat.getProductId()),
                        stat.getAverageRating(),
                        stat.getRatingCount()
                ))
                .collect(Collectors.toMap(ProductReviewStats::productId, Function.identity()));
    }
}
