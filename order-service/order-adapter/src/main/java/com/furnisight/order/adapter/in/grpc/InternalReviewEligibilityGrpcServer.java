package com.furnisight.order.adapter.in.grpc;

import com.furnisight.internal.order.InternalReviewEligibilityServiceGrpc;
import com.furnisight.internal.order.ValidateReviewEligibilityRequest;
import com.furnisight.internal.order.ValidateReviewEligibilityResponse;
import com.furnisight.order.application.order.port.in.usecase.GetOrderQuery;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class InternalReviewEligibilityGrpcServer extends InternalReviewEligibilityServiceGrpc.InternalReviewEligibilityServiceImplBase {

    private final GetOrderQuery getOrderQuery;

    @Override
    public void validateReviewEligibility(
            ValidateReviewEligibilityRequest request,
            StreamObserver<ValidateReviewEligibilityResponse> responseObserver
    ) {
        boolean valid = false;

        try {
            UUID userId = UUID.fromString(request.getUserId());
            UUID expectedOrderItemId = request.getOrderItemId().isBlank()
                    ? null : UUID.fromString(request.getOrderItemId());

            UUID deliveredOrderItemId = getOrderQuery
                    .getDeliveredOrderItemIdForProduct(userId, request.getProductId())
                    .orElse(null);

            valid = deliveredOrderItemId != null
                    && (expectedOrderItemId == null || deliveredOrderItemId.equals(expectedOrderItemId));
        } catch (Exception ignored) {
            valid = false;
        }

        responseObserver.onNext(ValidateReviewEligibilityResponse.newBuilder()
                .setValid(valid)
                .setReason(valid ? "" : "Order item is not eligible for review")
                .build());
        responseObserver.onCompleted();
    }
}
