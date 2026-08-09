package com.furnisight.review.adapter.out.client;

import com.furnisight.internal.order.InternalReviewEligibilityServiceGrpc;
import com.furnisight.internal.order.ValidateReviewEligibilityRequest;
import com.furnisight.review.application.review.port.out.ReviewEligibilityPort;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderReviewEligibilityClient implements ReviewEligibilityPort {

    @GrpcClient("order-service")
    private InternalReviewEligibilityServiceGrpc.InternalReviewEligibilityServiceBlockingStub reviewEligibilityStub;

    @Override
    public boolean isEligible(UUID userId, UUID productId, UUID orderItemId) {
        return reviewEligibilityStub.validateReviewEligibility(
                ValidateReviewEligibilityRequest.newBuilder()
                        .setUserId(userId.toString())
                        .setProductId(productId.toString())
                        .setOrderItemId(orderItemId.toString())
                        .build()
        ).getValid();
    }
}
