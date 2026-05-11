package com.furnisight.notification.application.notification.service;

import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.application.notification.port.out.sender.NotificationSender;
import com.furnisight.notification.domain.exception.UnsupportedNotificationChannelException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotificationService implements SendNotificationUseCase {

    private final List<NotificationSender> senders;

    @Override
    public Void execute(SendNotificationCommand command) {

        NotificationSender sender = senders.stream()
                .filter(s -> s.getChannel() == command.getChannel())
                .findFirst()
                .orElseThrow(() -> new UnsupportedNotificationChannelException(command.getChannel()));

        sender.send(command);
        return null;
    }
}
