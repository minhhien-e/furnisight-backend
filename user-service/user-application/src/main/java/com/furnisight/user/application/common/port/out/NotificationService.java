package com.furnisight.user.application.common.port.out;

public interface NotificationService {
    void createNotificationProfile(String accountId);

    void deleteNotificationProfile(String accountId);
}
