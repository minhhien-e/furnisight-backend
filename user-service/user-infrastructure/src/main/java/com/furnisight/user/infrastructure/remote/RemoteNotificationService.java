package com.furnisight.user.infrastructure.remote;

import com.furnisight.user.application.common.port.out.NotificationService;
import com.furnisight.user.infrastructure.grpc.GrpcNotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoteNotificationService implements NotificationService {
    private final GrpcNotificationClient grpcNotificationClient;

    @Override
    public void createNotificationProfile(String accountId) {
        grpcNotificationClient.createNotificationProfile(accountId);
    }

    @Override
    public void deleteNotificationProfile(String accountId) {
    }
}
