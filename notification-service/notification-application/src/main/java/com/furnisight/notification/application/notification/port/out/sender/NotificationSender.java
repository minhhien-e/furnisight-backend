package com.furnisight.notification.application.notification.port.out.sender;

import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.domain.model.enums.NotificationChannel;

public interface NotificationSender {
    NotificationChannel getChannel();
    void send(SendNotificationCommand command);
}
