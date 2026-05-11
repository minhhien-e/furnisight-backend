package com.furnisight.notification.application.inbox.service;

import com.furnisight.notification.application.inbox.port.in.dto.command.DeleteInboxMessageCommand;
import com.furnisight.notification.application.inbox.port.in.usecase.DeleteInboxMessageUseCase;
import com.furnisight.notification.application.inbox.port.out.repository.InboxMessageRepository;
import com.furnisight.notification.domain.exception.InboxMessageNotFoundException;
import com.furnisight.notification.domain.model.entity.InboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteInboxMessageService implements DeleteInboxMessageUseCase {

    private final InboxMessageRepository inboxMessageRepository;

    @Override
    @Transactional
    public Void execute(DeleteInboxMessageCommand command) {
        if (command.isHardDelete()) {
            inboxMessageRepository.deleteById(command.getMessageId());
        } else {
            InboxMessage message = inboxMessageRepository.findById(command.getMessageId())
                    .orElseThrow(() -> new InboxMessageNotFoundException(command.getMessageId()));
            message.markAsDeleted();
            inboxMessageRepository.save(message);
        }
        return null;
    }
}
