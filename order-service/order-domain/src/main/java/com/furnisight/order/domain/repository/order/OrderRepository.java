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
    List<Order> findAllByStatus(OrderStatus status, int page, int size);
    List<Order> findAllByStatusesAndCreatedAtBefore(List<OrderStatus> statuses, LocalDateTime cutoff);
    List<Order> findAll();
    List<Order> findAll(int page, int size);
    long countAll();
    long countByStatus(OrderStatus status);
    long countCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatusCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);
    double sumTotalAmount();
    double sumTotalAmountCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<TopSellingProductQuery> findTopSellingProducts(int limit);
    Optional<UUID> findDeliveredOrderItemIdByUserIdAndProductId(UUID userId, String productId);
}
