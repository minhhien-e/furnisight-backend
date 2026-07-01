package com.furnisight.notification.application.notification.service;

import com.furnisight.notification.application.inbox.port.out.repository.InboxMessageRepository;
import com.furnisight.notification.application.notification.port.in.dto.command.ReceiveNotificationCommand;
import com.furnisight.notification.application.notification.port.in.dto.command.ValidateNotificationProfileCommand;
import com.furnisight.notification.application.notification.port.in.usecase.ReceiveNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.ValidateNotificationProfileUseCase;
import com.furnisight.notification.domain.exceptions.ForbiddenException;
import com.furnisight.notification.domain.exceptions.ErrorCode;
import com.furnisight.notification.domain.model.entity.InboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiveNotificationService implements ReceiveNotificationUseCase {

    private final InboxMessageRepository inboxMessageRepository;
    private final ValidateNotificationProfileUseCase validateNotificationProfileUseCase;

    @Override
    @Transactional
    public Void execute(ReceiveNotificationCommand command) {
        boolean isAllowed = validateNotificationProfileUseCase.execute(
            ValidateNotificationProfileCommand.builder()
                .userId(command.getUserId())
                .notificationType(command.getType())
                .build()
        );

        if (!isAllowed) {
            throw new ForbiddenException(ErrorCode.NOTIFICATION_PROFILE_NOT_ALLOWED);
        }

        InboxMessage message = InboxMessage.builder()
            .userId(command.getUserId())
            .title(command.getTitle())
            .body(command.getBody())
            .image(command.getImage())
            .actionUrl(command.getActionUrl())
            .type(command.getType())
            .build();

        inboxMessageRepository.save(message);
        return null;
    }
}
