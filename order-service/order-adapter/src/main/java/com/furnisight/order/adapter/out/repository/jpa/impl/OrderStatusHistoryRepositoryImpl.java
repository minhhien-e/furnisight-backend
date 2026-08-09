package com.furnisight.order.adapter.out.repository.jpa.impl;

import com.furnisight.order.adapter.out.repository.jpa.OrderStatusHistoryJpaRepository;
import com.furnisight.order.domain.entities.order.OrderStatusHistory;
import com.furnisight.order.domain.repository.order.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderStatusHistoryRepositoryImpl implements OrderStatusHistoryRepository {
    private final OrderStatusHistoryJpaRepository repository;

    @Override
    public OrderStatusHistory save(OrderStatusHistory history) {
        return repository.save(history);
    }

    @Override
    public List<OrderStatusHistory> findByOrderId(UUID orderId) {
        return repository.findAllByOrderIdOrderByCreatedAtAsc(orderId);
    }
}
