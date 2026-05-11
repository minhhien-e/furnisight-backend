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
    public void createNotificationProfile(String accountId, String email) {
        grpcNotificationClient.createNotificationProfile(accountId, email);
    }

    @Override
    public void updateNotificationProfile(String accountId, String email) {
        grpcNotificationClient.updateNotificationProfile(accountId, email);
    }

    @Override
    public void deleteNotificationProfile(String accountId) {
        grpcNotificationClient.deleteNotificationProfile(accountId);
    }
}
