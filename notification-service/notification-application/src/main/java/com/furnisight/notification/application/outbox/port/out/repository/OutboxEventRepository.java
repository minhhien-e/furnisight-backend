package com.furnisight.notification.application.outbox.port.out.repository;

import com.furnisight.notification.domain.model.entity.OutboxEvent;
import com.furnisight.notification.domain.model.enums.OutboxStatus;

import java.util.List;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> saveAll(List<OutboxEvent> outboxEvents);

    List<OutboxEvent> findAllByStatus(OutboxStatus status, int limit);
}
