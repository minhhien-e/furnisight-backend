package com.furnisight.order.domain.entities.order;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.seedwork.DomainEntity;
import com.furnisight.order.domain.valueobjects.OrderFee;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import com.furnisight.order.domain.valueobjects.ShippingDetail;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends DomainEntity {
    @Id
    private UUID id;
    private UUID userId;
    private String orderCode;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Double subTotal;
    private Double totalAmount;
    @Embedded
    private OrderFee fee;

    private Double savedAmount;
    private String customerNote;
    private String trackingCode;

    @Embedded
    private ShippingDetail shippingDetail;

    @Embedded
    private PaymentDetail paymentDetail;

    @Embedded
    private com.furnisight.order.domain.valueobjects.PaymentTimeline paymentTimeline;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(UUID id, UUID userId, String orderCode, OrderStatus status, OrderFee fee, String customerNote,
                 ShippingDetail shippingDetail, PaymentDetail paymentDetail, com.furnisight.order.domain.valueobjects.PaymentTimeline paymentTimeline, List<OrderItem> items) {

        if (items == null || items.isEmpty()) {
            throw new ValidationException(
                    ErrorCode.ORDER_ITEM_EMPTY);
        }

        this.id = id;
        this.userId = userId;
        this.orderCode = orderCode;
        this.status = status;
        this.fee = fee;
        this.customerNote = customerNote;
        this.shippingDetail = shippingDetail;
        this.paymentDetail = paymentDetail;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.paymentTimeline = paymentTimeline != null ? paymentTimeline : com.furnisight.order.domain.valueobjects.PaymentTimeline.builder().orderCreatedAt(now).build();
        this.items = new ArrayList<>();

        for (OrderItem item : items) {
            item.setOrder(this);
            this.items.add(item);
        }
        
        this.addDomainEvent(com.furnisight.order.domain.events.OrderCreatedEvent.builder()
                .orderCode(this.orderCode)
                .build());
    }

    public void markPaymentInitiated(java.time.LocalDateTime initiatedAt) {
        if (this.status != OrderStatus.UNPAID && this.status != OrderStatus.PAYMENT_FAILED) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        if (this.paymentTimeline != null) {
            this.paymentTimeline = this.paymentTimeline.initiatePayment(initiatedAt);
        } else {
            this.paymentTimeline = com.furnisight.order.domain.valueobjects.PaymentTimeline.builder()
                    .orderCreatedAt(this.createdAt != null ? this.createdAt : java.time.LocalDateTime.now())
                    .paymentInitiatedAt(initiatedAt)
                    .build();
        }
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void markAsPaid(String paymentMethod, Double paidAmount, java.time.LocalDateTime paidAt) {
        if (this.status != OrderStatus.UNPAID && this.status != OrderStatus.PAYMENT_FAILED) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.PAID;
        this.paymentDetail = PaymentDetail.builder()
                .paymentMethod(paymentMethod)
                .paymentStatus("PAID")
                .paidAmount(paidAmount)
                .paidAt(paidAt)
                .build();
        
        if (this.paymentTimeline != null) {
            this.paymentTimeline = this.paymentTimeline.completePayment(paidAt);
        } else {
            this.paymentTimeline = com.furnisight.order.domain.valueobjects.PaymentTimeline.builder()
                    .orderCreatedAt(this.createdAt != null ? this.createdAt : java.time.LocalDateTime.now())
                    .paymentCompletedAt(paidAt)
                    .build();
        }
        this.updatedAt = java.time.LocalDateTime.now();
        this.addDomainEvent(com.furnisight.order.domain.events.OrderPaidEvent.builder()
                .orderCode(this.orderCode)
                .paidAmount(paidAmount)
                .paymentMethod(paymentMethod)
                .build());
    }

    public void markPaymentFailed(String paymentMethod, java.time.LocalDateTime failedAt, String reason) {
        if (this.status != OrderStatus.UNPAID && this.status != OrderStatus.PAYMENT_FAILED) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        this.paymentDetail = PaymentDetail.builder()
                .paymentMethod(paymentMethod)
                .paymentStatus("FAILED")
                .paidAmount(0.0)
                .paidAt(null)
                .build();
        
        if (this.paymentTimeline != null) {
            this.paymentTimeline = this.paymentTimeline.failPayment(failedAt);
        } else {
            this.paymentTimeline = com.furnisight.order.domain.valueobjects.PaymentTimeline.builder()
                    .orderCreatedAt(this.createdAt != null ? this.createdAt : java.time.LocalDateTime.now())
                    .paymentFailedAt(failedAt)
                    .build();
        }
        this.updatedAt = java.time.LocalDateTime.now();
        this.addDomainEvent(com.furnisight.order.domain.events.OrderPaymentFailedEvent.builder()
                .orderCode(this.orderCode)
                .build());
    }

    public void shipOrder() {
        if (this.status != OrderStatus.PAID && !isUnpaidCodOrder()) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.SHIPPING;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    private boolean isUnpaidCodOrder() {
        return this.status == OrderStatus.UNPAID
                && this.paymentDetail != null
                && "cod".equalsIgnoreCase(this.paymentDetail.getPaymentMethod());
    }

    public void deliverOrder() {
        boolean canCompleteCod = isCodOrder()
                && (this.status == OrderStatus.UNPAID || this.status == OrderStatus.PAID);
        if (this.status != OrderStatus.SHIPPING && !canCompleteCod) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void cancelOrder() {
        if (this.status == OrderStatus.SHIPPING
                || this.status == OrderStatus.DELIVERED
                || this.status == OrderStatus.CANCELLED
                || this.status == OrderStatus.REFUND_PENDING
                || this.status == OrderStatus.REFUNDED) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = this.status == OrderStatus.PAID && !isCodOrder()
                ? OrderStatus.REFUND_PENDING
                : OrderStatus.CANCELLED;
        this.updatedAt = java.time.LocalDateTime.now();
        this.addDomainEvent(com.furnisight.order.domain.events.OrderCancelledEvent.builder()
                .orderCode(this.orderCode)
                .build());
    }

    public void markAsRefunded() {
        if (this.status != OrderStatus.REFUND_PENDING) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.REFUNDED;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void transitionTo(OrderStatus nextStatus) {
        this.status = nextStatus;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void recordCancellation() {
        this.addDomainEvent(com.furnisight.order.domain.events.OrderCancelledEvent.builder()
                .orderCode(this.orderCode)
                .build());
    }

    public void recordPaymentInitiated(java.time.LocalDateTime initiatedAt) {
        this.paymentTimeline = this.paymentTimeline == null
                ? com.furnisight.order.domain.valueobjects.PaymentTimeline.builder()
                    .orderCreatedAt(this.createdAt)
                    .paymentInitiatedAt(initiatedAt)
                    .build()
                : this.paymentTimeline.initiatePayment(initiatedAt);
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void recordPaymentSuccess(String method, Double amount, java.time.LocalDateTime paidAt) {
        this.paymentDetail = PaymentDetail.builder()
                .paymentMethod(method)
                .paymentStatus("PAID")
                .paidAmount(amount)
                .paidAt(paidAt)
                .build();
        this.paymentTimeline = this.paymentTimeline == null
                ? com.furnisight.order.domain.valueobjects.PaymentTimeline.builder()
                    .orderCreatedAt(this.createdAt)
                    .paymentCompletedAt(paidAt)
                    .build()
                : this.paymentTimeline.completePayment(paidAt);
        this.updatedAt = java.time.LocalDateTime.now();
        this.addDomainEvent(com.furnisight.order.domain.events.OrderPaidEvent.builder()
                .orderCode(this.orderCode)
                .paidAmount(amount)
                .paymentMethod(method)
                .build());
    }

    public void recordPaymentFailure(String method, java.time.LocalDateTime failedAt) {
        this.paymentDetail = PaymentDetail.builder()
                .paymentMethod(method)
                .paymentStatus("FAILED")
                .paidAmount(0.0)
                .build();
        this.paymentTimeline = this.paymentTimeline == null
                ? com.furnisight.order.domain.valueobjects.PaymentTimeline.builder()
                    .orderCreatedAt(this.createdAt)
                    .paymentFailedAt(failedAt)
                    .build()
                : this.paymentTimeline.failPayment(failedAt);
        this.updatedAt = java.time.LocalDateTime.now();
        this.addDomainEvent(com.furnisight.order.domain.events.OrderPaymentFailedEvent.builder()
                .orderCode(this.orderCode)
                .build());
    }

    public void updateTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode == null || trackingCode.isBlank() ? null : trackingCode.trim();
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public boolean isCodOrder() {
        return this.paymentDetail != null
                && "cod".equalsIgnoreCase(this.paymentDetail.getPaymentMethod());
    }
}
