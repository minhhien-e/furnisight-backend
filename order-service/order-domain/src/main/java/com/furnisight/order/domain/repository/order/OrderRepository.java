package com.furnisight.order.domain.repository.order;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findAllByUserId(UUID userId);
    List<Order> findAllByStatus(OrderStatus status);
    List<Order> findAllByStatusesAndCreatedAtBefore(List<OrderStatus> statuses, LocalDateTime cutoff);
    List<Order> findAll();
}
