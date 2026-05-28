package com.furnisight.order.adapter.out.repository.jpa.impl;

import com.furnisight.order.adapter.out.repository.jpa.OutboxMessageJpaRepository;
import com.furnisight.order.domain.entities.OutboxMessage;
import com.furnisight.order.domain.repository.OutboxMessageRepository;
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
    public List<OutboxMessage> findPendingMessages(int batchSize) {
        return jpaRepository.findPendingMessages(PageRequest.of(0, batchSize));
    }
}
