package com.furnisight.order.adapter.out.repository.jpa.impl;

import com.furnisight.order.adapter.out.repository.jpa.OrderJpaRepository;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.entities.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public java.util.List<Order> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Optional<Order> findByOrderCode(String orderCode) {
        return jpaRepository.findByOrderCode(orderCode);
    }

    @Override
    public java.util.List<Order> findAllByStatus(com.furnisight.order.domain.enums.OrderStatus status) {
        return jpaRepository.findAllByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public java.util.List<Order> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc();
    }
}
