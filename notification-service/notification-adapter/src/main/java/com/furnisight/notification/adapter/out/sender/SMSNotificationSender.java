package com.furnisight.notification.adapter.out.sender;

import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.out.sender.NotificationSender;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SMSNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(SendNotificationCommand command) {
        log.info("Sending SMS notification to user: {}. Body: {}", command.getUserId(), command.getBody());
        // Integration with SMS service would go here
    }
}
