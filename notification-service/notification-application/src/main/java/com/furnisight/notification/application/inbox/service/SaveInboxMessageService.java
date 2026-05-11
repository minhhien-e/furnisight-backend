package com.furnisight.notification.application.inbox.service;

import com.furnisight.notification.application.inbox.port.in.dto.command.SaveInboxMessageCommand;
import com.furnisight.notification.application.inbox.port.in.dto.projection.InboxMessageProjection;
import com.furnisight.notification.application.inbox.port.in.usecase.SaveInboxMessageUseCase;
import com.furnisight.notification.application.inbox.port.out.repository.InboxMessageRepository;
import com.furnisight.notification.application.outbox.port.in.usecase.AddOutboxEventUseCase;
import com.furnisight.notification.domain.model.entity.InboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveInboxMessageService implements SaveInboxMessageUseCase {

    private final InboxMessageRepository inboxMessageRepository;
    private final AddOutboxEventUseCase addOutboxEventUseCase;

    @Override
    @Transactional
    public InboxMessageProjection execute(SaveInboxMessageCommand command) {
        InboxMessage message = InboxMessage.builder()
            .id(UUID.randomUUID())
            .userId(command.getUserId())
            .title(command.getTitle())
            .body(command.getBody())
            .image(command.getImage())
            .actionUrl(command.getActionUrl())
            .type(command.getType())
            .build();

        addOutboxEventUseCase.addDomainEvents(message.getDomainEvents());
        message.clearDomainEvents();

        var saved = inboxMessageRepository.save(message);
        return InboxMessageProjection.from(saved);
    }
}
