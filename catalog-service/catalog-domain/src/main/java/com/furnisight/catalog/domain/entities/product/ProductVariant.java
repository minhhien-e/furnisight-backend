package com.furnisight.catalog.domain.entities.product;

import lombok.*;
import jakarta.persistence.*;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.SKU;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
import com.furnisight.catalog.domain.exceptions.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "sku", nullable = false, unique = true))
    private SKU sku;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price", nullable = false, precision = 19, scale = 4))
    private Price price;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "stock_quantity", nullable = false))
    private StockQuantity stockQuantity;

    // Decrease stock when there's an order
    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Quantity to subtract must be greater than 0");
        }

        if (this.stockQuantity.getValue() < quantity) {
            throw new InvalidOperationException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stockQuantity = this.stockQuantity.decrease(quantity);
    }

    // Add stock when restocked
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Quantity to add must be greater than 0");
        }
        this.stockQuantity = this.stockQuantity.increase(quantity);
    }
}
