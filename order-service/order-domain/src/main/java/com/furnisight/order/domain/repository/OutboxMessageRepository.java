package com.furnisight.order.domain.repository;

import com.furnisight.order.domain.entities.OutboxMessage;
import java.util.List;

public interface OutboxMessageRepository {
    OutboxMessage save(OutboxMessage outboxMessage);
    List<OutboxMessage> findPendingMessages(int batchSize);
}
