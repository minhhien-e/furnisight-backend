package com.furnisight.notification.adapter.out.repository.jpa.impl;

import com.furnisight.notification.adapter.out.repository.jpa.NotificationLogJpaRepository;
import com.furnisight.notification.application.notification.port.out.repository.NotificationLogRepository;
import com.furnisight.notification.domain.model.entity.NotificationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationLogRepositoryImpl implements NotificationLogRepository {

    private final NotificationLogJpaRepository jpaRepository;

    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        return jpaRepository.save(notificationLog);
    }

    @Override
    public Optional<NotificationLog> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
