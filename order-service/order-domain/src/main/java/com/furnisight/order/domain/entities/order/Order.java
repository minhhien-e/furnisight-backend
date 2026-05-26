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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(UUID id, UUID userId, String orderCode, OrderStatus status, OrderFee fee, String customerNote,
                 ShippingDetail shippingDetail, PaymentDetail paymentDetail, List<OrderItem> items) {

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
}
