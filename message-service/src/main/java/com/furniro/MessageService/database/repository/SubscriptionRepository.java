package com.furniro.MessageService.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.furniro.MessageService.database.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    boolean existsByEmail(String email);

    Page<Subscription> findAll(Pageable pageable);
}