package com.furnisight.order.adapter.out.repository.jpa;

import com.furnisight.order.domain.entities.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistory, UUID> {
    List<OrderStatusHistory> findAllByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
