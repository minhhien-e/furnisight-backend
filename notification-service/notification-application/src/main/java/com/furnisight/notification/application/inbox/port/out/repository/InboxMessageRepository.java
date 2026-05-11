package com.furnisight.notification.application.inbox.port.out.repository;

import com.furnisight.notification.domain.model.entity.InboxMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InboxMessageRepository {
    InboxMessage save(InboxMessage entity);
    Optional<InboxMessage> findById(UUID id);
    void deleteById(UUID id);
    void markAllAsReadByUserId(UUID userId);
    List<InboxMessage> findAllByUserId(UUID userId, boolean deleted, LocalDateTime startDate, int limit);
}
