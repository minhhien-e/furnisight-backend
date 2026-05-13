package com.furnisight.catalog.domain.entities.product;

import com.furnisight.catalog.domain.exceptions.InsufficientStockException;
import lombok.*;
import jakarta.persistence.*;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.SKU;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Embedded
    private SKU sku;

    @Embedded
    private Price price;

    @Embedded
    private StockQuantity stockQuantity;

    // Decrease stock when there's an order
    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity to subtract must be greater than 0");
        }

        if (this.stockQuantity.getValue() < quantity) {
            throw new InsufficientStockException(this.sku.getValue(), this.stockQuantity.getValue(), quantity);
        }
        this.stockQuantity = this.stockQuantity.decrease(quantity);
    }

    // Add stock when restocked
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be greater than 0");
        }
        this.stockQuantity = this.stockQuantity.increase(quantity);
    }
}
