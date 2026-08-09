package com.furnisight.order.domain.entities.order;

import com.furnisight.order.domain.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory {
    @Id
    private UUID id;
    private UUID orderId;
    private String orderCode;
    @Enumerated(EnumType.STRING)
    private OrderStatus previousStatus;
    @Enumerated(EnumType.STRING)
    private OrderStatus nextStatus;
    private UUID actorId;
    private String actorType;
    private String trackingCode;
    private String note;
    private LocalDateTime createdAt;

    @Builder
    public OrderStatusHistory(UUID orderId, String orderCode, OrderStatus previousStatus,
                              OrderStatus nextStatus, UUID actorId, String actorType,
                              String trackingCode, String note, LocalDateTime createdAt) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.previousStatus = previousStatus;
        this.nextStatus = nextStatus;
        this.actorId = actorId;
        this.actorType = actorType;
        this.trackingCode = trackingCode;
        this.note = note;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }
}
