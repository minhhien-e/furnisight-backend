package com.furnisight.order.domain.entities.order;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.seedwork.DomainEntity;
import com.furnisight.order.domain.valueobjects.ProductSnapshot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends DomainEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Embedded
    private ProductSnapshot productSnapshot;

    // "8.500.000đ"
    private Double price;

    // Original price (scratched out) "1.450.000đ"
    private Double oldPrice;

    // Quantity "1"
    private Integer quantity;

    @Builder
    public OrderItem(UUID id, Order order, ProductSnapshot productSnapshot, Double price, Double oldPrice, Integer quantity) {
        if (price == null || price < 0) {
            throw new ValidationException(ErrorCode.NEGATIVE_PRICE);
        }
        if (quantity == null || quantity <= 0) {
            throw new ValidationException(ErrorCode.INVALID_QUANTITY);
        }

        this.id = id;
        this.order = order;
        this.productSnapshot = productSnapshot;
        this.price = price;
        this.oldPrice = oldPrice;
        this.quantity = quantity;
    }
}
