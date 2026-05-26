package com.furnisight.order.adapter.out.repository.jpa;

import com.furnisight.order.domain.entities.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    java.util.Optional<Order> findByOrderCode(String orderCode);
    List<Order> findAllByStatusOrderByCreatedAtDesc(com.furnisight.order.domain.enums.OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
}
