package com.furnisight.user.infrastructure.database.repository.impl;

import com.furnisight.user.domain.entities.OutboxMessage;
import com.furnisight.user.domain.repository.OutboxMessageRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.OutboxMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxMessageRepositoryImpl implements OutboxMessageRepository {

    private final OutboxMessageJpaRepository jpaRepository;

    @Override
    public OutboxMessage save(OutboxMessage outboxMessage) {
        return jpaRepository.save(outboxMessage);
    }

    @Override
    public List<OutboxMessage> findPendingMessages() {
        return jpaRepository.findPendingMessages(PageRequest.of(0, 100));
    }

    @Override
    public void delete(OutboxMessage outboxMessage) {
        jpaRepository.delete(outboxMessage);
    }
}
