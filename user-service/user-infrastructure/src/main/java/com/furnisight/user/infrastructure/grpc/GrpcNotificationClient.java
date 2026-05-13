package com.furnisight.user.infrastructure.grpc;

import com.furnisight.notification.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class GrpcNotificationClient {

    @GrpcClient("notification-service")
    private NotificationServiceGrpc.NotificationServiceBlockingStub notificationServiceBlockingStub;

    public CreateNotificationProfileResponse createNotificationProfile(String accountId) {
        CreateNotificationProfileRequest request = CreateNotificationProfileRequest.newBuilder()
                .setAccountId(accountId)
                .build();
        return notificationServiceBlockingStub.createNotificationProfile(request);
    }
}
