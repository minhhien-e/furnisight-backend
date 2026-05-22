package com.furnisight.order.domain.entities.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.*;
import com.furnisight.order.domain.seedwork.DomainEntity;
import jakarta.persistence.Embedded;
import com.furnisight.order.domain.valueobjects.order.OrderFee;
import com.furnisight.order.domain.valueobjects.order.ShippingDetail;
import com.furnisight.order.domain.valueobjects.order.PaymentDetail;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends DomainEntity{
    @Id
    private UUID id;
    private UUID userId;
    private String status;
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
    public Order(UUID id, UUID userId, String status, OrderFee fee, String customerNote, 
                 ShippingDetail shippingDetail, PaymentDetail paymentDetail, List<OrderItem> items) {
        
        if (items == null || items.isEmpty()) {
            throw new com.furnisight.order.domain.exceptions.order.ValidationException(
                    com.furnisight.order.domain.exceptions.order.ErrorCode.ORDER_ITEM_EMPTY);
        }

        this.id = id;
        this.userId = userId;
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
