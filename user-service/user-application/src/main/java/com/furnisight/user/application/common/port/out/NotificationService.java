package com.furnisight.user.application.common.port.out;

public interface NotificationService {
    void createNotificationProfile(String accountId, String email);

    void updateNotificationProfile(String accountId, String email);

    void deleteNotificationProfile(String accountId);
}
