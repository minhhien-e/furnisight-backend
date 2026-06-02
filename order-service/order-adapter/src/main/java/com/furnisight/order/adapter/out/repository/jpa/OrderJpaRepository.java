package com.furnisight.order.adapter.out.repository.jpa;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
    @EntityGraph(attributePaths = "items")
    List<Order> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "items")
    java.util.Optional<Order> findByOrderCode(String orderCode);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o where o.status in :statuses and (o.createdAt is null or o.createdAt <= :cutoff)")
    List<Order> findAllByStatusInAndCreatedAtBefore(@Param("statuses") List<OrderStatus> statuses, @Param("cutoff") LocalDateTime cutoff);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(OrderStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o")
    Double sumTotalAmount();

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.createdAt between :start and :end")
    Double sumTotalAmountCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
