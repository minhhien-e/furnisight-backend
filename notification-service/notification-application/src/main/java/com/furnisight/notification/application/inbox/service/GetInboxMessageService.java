package com.furnisight.notification.application.inbox.service;

import com.furnisight.notification.application.inbox.port.in.dto.projection.InboxMessageProjection;
import com.furnisight.notification.application.inbox.port.in.dto.query.GetInboxMessageQuery;
import com.furnisight.notification.application.inbox.port.in.usecase.GetInboxMessageUseCase;
import com.furnisight.notification.application.inbox.port.out.repository.InboxMessageRepository;
import com.furnisight.notification.domain.model.entity.InboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetInboxMessageService implements GetInboxMessageUseCase {
    private final InboxMessageRepository inboxMessageRepository;

    @Override
    public List<InboxMessageProjection> execute(GetInboxMessageQuery command) {
        List<InboxMessage> messages = inboxMessageRepository.findAllByUserId(command.getUserId(), command.isDeleted(),command.getStartDate(), command.getLimit());
        return messages.stream().map(InboxMessageProjection::from).toList();
    }
}
