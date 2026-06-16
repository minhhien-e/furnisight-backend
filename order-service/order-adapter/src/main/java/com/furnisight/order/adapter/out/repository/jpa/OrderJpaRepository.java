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

    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o where o.status in :statuses and (o.createdAt is null or o.createdAt <= :cutoff)")
    List<Order> findAllByStatusInAndCreatedAtBefore(@Param("statuses") List<OrderStatus> statuses, @Param("cutoff") LocalDateTime cutoff);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(OrderStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o")
    Double sumTotalAmount();

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.createdAt between :start and :end")
    Double sumTotalAmountCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT oi.productSnapshot.productId AS productId, " +
                   "MAX(oi.productSnapshot.productName) AS productName, " +
                   "MAX(oi.productSnapshot.categoryName) AS categoryName, " +
                   "MAX(oi.productSnapshot.imageUrl) AS imageUrl, " +
                   "MAX(oi.price) AS price, " +
                   "SUM(oi.quantity) AS soldCount, " +
                   "SUM(oi.price * oi.quantity) AS totalRevenue " +
                   "FROM Order o JOIN o.items oi " +
                   "WHERE o.status NOT IN :statuses " +
                   "GROUP BY oi.productSnapshot.productId " +
                   "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    @Query("select oi.id " +
           "from Order o join o.items oi " +
           "where o.userId = :userId " +
           "and o.status = :status " +
           "and oi.productSnapshot.productId = :productId " +
           "order by o.createdAt desc")
    List<UUID> findDeliveredOrderItemIdsByUserIdAndProductId(
            @Param("userId") UUID userId,
            @Param("productId") String productId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
