package com.furnisight.cart.grpc;

import com.furnisight.cart.repository.CartRepository;
import com.furnisight.internal.cart.AbandonedCartUserIdsResponse;
import com.furnisight.internal.cart.InternalCartServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@GrpcService
@RequiredArgsConstructor
public class InternalCartGrpcService extends InternalCartServiceGrpc.InternalCartServiceImplBase {

    private final CartRepository cartRepository;

    @Value("${cart.marketing.abandoned-after-hours:24}")
    private long abandonedAfterHours;

    @Override
    public void getAbandonedCartUserIds(Empty request, StreamObserver<AbandonedCartUserIdsResponse> responseObserver) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(Math.max(1, abandonedAfterHours));
        
        var userIds = cartRepository.findAbandonedCartIds(cutoff).stream()
                .map(cart -> cart.getId().toString())
                .distinct()
                .toList();

        AbandonedCartUserIdsResponse response = AbandonedCartUserIdsResponse.newBuilder()
                .addAllUserIds(userIds)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
