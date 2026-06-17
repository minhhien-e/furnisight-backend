package com.furnisight.user.domain.repository;

import com.furnisight.user.domain.entities.OutboxMessage;

import java.util.List;

public interface OutboxMessageRepository {
    OutboxMessage save(OutboxMessage outboxMessage);

    /**
     * Retrieves a list of pending messages to be processed.
     * Conditions:
     * - Message is not yet processed (processedAt IS NULL)
     * - Message has not permanently failed (failed = false)
     * - Message is ready for retry based on exponential backoff (nextRetryAt IS
     * NULL OR nextRetryAt <= CURRENT_TIMESTAMP)
     * Limit: 100 messages ordered by creation time ascending.
     */
    List<OutboxMessage> findPendingMessages();

    void delete(OutboxMessage outboxMessage);
}
