package com.furnisight.notification.adapter.out.sender;

import com.furnisight.notification.application.inbox.port.in.dto.command.SaveInboxMessageCommand;
import com.furnisight.notification.application.inbox.port.in.usecase.SaveInboxMessageUseCase;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.out.sender.NotificationSender;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationSender implements NotificationSender {

    private final SaveInboxMessageUseCase saveInboxMessageUseCase;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void send(SendNotificationCommand command) {

        SaveInboxMessageCommand saveCommand = SaveInboxMessageCommand.builder()
                .userId(command.getUserId())
                .title(command.getTitle())
                .body(command.getBody())
                .image(command.getImage())
                .actionUrl(command.getActionUrl())
                .type(command.getType())
                .metadata(command.getMetadata())
                .build();

        saveInboxMessageUseCase.execute(saveCommand);
    }
}
