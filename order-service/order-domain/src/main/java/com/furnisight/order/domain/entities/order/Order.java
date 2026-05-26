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
        this.paymentTimeline = paymentTimeline != null ? paymentTimeline : com.furnisight.order.domain.valueobjects.PaymentTimeline.builder().orderCreatedAt(java.time.LocalDateTime.now()).build();
        this.items = new ArrayList<>();

        for (OrderItem item : items) {
            item.setOrder(this);
            this.items.add(item);
        }

        calculateTotals();
    }

    private void calculateTotals() {
        // Calculate Subtotal
        this.subTotal = this.items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Safe check for fees
        double shipping = fee != null && fee.getShippingFee() != null ? fee.getShippingFee() : 0.0;
        double shippingDisc = fee != null && fee.getShippingDiscount() != null ? fee.getShippingDiscount() : 0.0;
        double discount = fee != null && fee.getDiscountAmount() != null ? fee.getDiscountAmount() : 0.0;
        double insurance = fee != null && fee.getInsuranceFee() != null ? fee.getInsuranceFee() : 0.0;

        // Calculate Total
        this.totalAmount = this.subTotal + shipping + insurance - shippingDisc - discount;
        if (this.totalAmount < 0) {
            this.totalAmount = 0.0;
        }

        // Calculate Saved Amount (from old prices and discounts)
        double itemSaved = this.items.stream()
                .mapToDouble(item -> {
                    if (item.getOldPrice() != null && item.getOldPrice() > item.getPrice()) {
                        return (item.getOldPrice() - item.getPrice()) * item.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
        this.savedAmount = itemSaved + shippingDisc + discount;
    }

    public void markPaymentInitiated(java.time.LocalDateTime initiatedAt) {
        if (this.status != OrderStatus.UNPAID) {
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
        if (this.status != OrderStatus.UNPAID) {
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
    }

    public void shipOrder() {
        if (this.status != OrderStatus.PAID) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.SHIPPING;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void deliverOrder() {
        if (this.status != OrderStatus.SHIPPING) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void cancelOrder() {
        if (this.status == OrderStatus.SHIPPING || this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELLED) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
