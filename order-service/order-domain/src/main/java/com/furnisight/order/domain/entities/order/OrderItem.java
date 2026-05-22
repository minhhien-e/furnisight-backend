package com.furnisight.order.domain.entities.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import com.furnisight.order.domain.seedwork.DomainEntity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
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

    // Snapshot data from product/cart at the time of purchase
    private String productId;
    private String variantId;
    
    // UI displays "SOFA", "GHẾ", "BÀN TRÀ"
    private String categoryName;
    
    // Product name "Sofa Nordic 3 chỗ ngồi"
    private String productName;
    
    // Variant description "Be sáng / 2m1 x 95cm"
    private String variantDescription;
    
    // "8.500.000đ"
    private Double price;
    
    // Original price (scratched out) "1.450.000đ"
    private Double oldPrice;
    
    // Quantity "1"
    private Integer quantity;
    
    // Image URL for the product/variant
    private String imageUrl;

    @Builder
    public OrderItem(UUID id, Order order, String productId, String variantId, String categoryName, 
                     String productName, String variantDescription, Double price, Double oldPrice, 
                     Integer quantity, String imageUrl) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new com.furnisight.order.domain.exceptions.order.ValidationException(
                    com.furnisight.order.domain.exceptions.order.ErrorCode.ORDER_ITEM_EMPTY);
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        this.id = id;
        this.order = order;
        this.productId = productId;
        this.variantId = variantId;
        this.categoryName = categoryName;
        this.productName = productName;
        this.variantDescription = variantDescription;
        this.price = price;
        this.oldPrice = oldPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }
}
