package com.furnisight.notification.application.inbox.service;

import com.furnisight.notification.application.inbox.port.in.dto.command.MarkAllInboxMessagesAsReadCommand;
import com.furnisight.notification.application.inbox.port.in.usecase.MarkAllInboxMessagesAsReadUseCase;
import com.furnisight.notification.application.inbox.port.out.repository.InboxMessageRepository;
import com.furnisight.notification.domain.exception.InboxMessageNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkAllInboxMessagesAsReadService implements MarkAllInboxMessagesAsReadUseCase {

    private final InboxMessageRepository inboxMessageRepository;

    @Override
    @Transactional
    public Void execute(MarkAllInboxMessagesAsReadCommand command) {
        inboxMessageRepository.markAllAsReadByUserId(command.getUserId());
        return null;
    }
}
