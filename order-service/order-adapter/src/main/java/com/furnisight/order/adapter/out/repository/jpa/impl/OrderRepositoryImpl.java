package com.furnisight.order.adapter.out.repository.jpa.impl;

import com.furnisight.order.adapter.out.repository.jpa.OrderJpaRepository;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
    public List<Order> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Optional<Order> findByOrderCode(String orderCode) {
        return jpaRepository.findByOrderCode(orderCode);
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status) {
        return jpaRepository.findAllByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status, int page, int size) {
        return jpaRepository.findAllByStatusOrderByCreatedAtDesc(status, PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
    }

    @Override
    public List<Order> findAllByStatusesAndCreatedAtBefore(List<OrderStatus> statuses, LocalDateTime cutoff) {
        return jpaRepository.findAllByStatusInAndCreatedAtBefore(statuses, cutoff);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Order> findAll(int page, int size) {
        return jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public long countCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public long countByStatusCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.countByStatusAndCreatedAtBetween(status, start, end);
    }

    @Override
    public double sumTotalAmount() {
        Double value = jpaRepository.sumTotalAmount();
        return value == null ? 0D : value;
    }

    @Override
    public double sumTotalAmountCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        Double value = jpaRepository.sumTotalAmountCreatedAtBetween(start, end);
        return value == null ? 0D : value;
    }

    @Override
    public List<Object[]> findTopSellingProducts(int limit) {
        return jpaRepository.findTopSellingProducts(
            List.of(OrderStatus.CANCELLED, OrderStatus.PAYMENT_FAILED, OrderStatus.REFUND_PENDING, OrderStatus.REFUNDED),
            PageRequest.of(0, Math.max(limit, 1))
        );
    }

    @Override
    public Optional<UUID> findDeliveredOrderItemIdByUserIdAndProductId(UUID userId, String productId) {
        return jpaRepository.findDeliveredOrderItemIdsByUserIdAndProductId(
                userId,
                productId,
                OrderStatus.DELIVERED,
                PageRequest.of(0, 1)
        ).stream().findFirst();
    }
}
