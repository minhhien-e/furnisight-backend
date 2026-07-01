package com.furnisight.notification.application.inbox.service;

import com.furnisight.notification.application.inbox.port.in.dto.command.MarkInboxMessageAsReadCommand;
import com.furnisight.notification.application.inbox.port.in.usecase.MarkInboxMessageAsReadUseCase;
import com.furnisight.notification.application.inbox.port.out.repository.InboxMessageRepository;
import com.furnisight.notification.domain.model.entity.InboxMessage;
import com.furnisight.notification.domain.exceptions.NotFoundException;
import com.furnisight.notification.domain.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MarkInboxMessageAsReadService implements MarkInboxMessageAsReadUseCase {

    private final InboxMessageRepository inboxMessageRepository;

    @Override
    @Transactional
    public Void execute(MarkInboxMessageAsReadCommand command) {
        InboxMessage message = inboxMessageRepository.findById(command.getMessageId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.INBOX_MESSAGE_NOT_FOUND));

        if (!message.isRead()) {
            message.markAsRead();
            inboxMessageRepository.save(message);
        }
        return null;
    }
}
