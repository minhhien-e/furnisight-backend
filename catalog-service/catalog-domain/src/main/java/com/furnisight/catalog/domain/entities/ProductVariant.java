package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.seedwork.BaseEntity;
import com.furnisight.catalog.domain.valueobjects.product.ProductDimensions;
import lombok.*;
import jakarta.persistence.*;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
import com.furnisight.catalog.domain.exceptions.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Embedded
    private Price price;

    @Embedded
    private StockQuantity stockQuantity;

    @Embedded
    private ProductDimensions dimensions;

    public ProductVariant(Product product, Price price, StockQuantity stockQuantity, ProductDimensions dimensions) {
        this.id = UUID.randomUUID();
        this.stockQuantity = new StockQuantity(0);
        update(product, price, stockQuantity, dimensions);
    }

    public void update(Product product, Price price, StockQuantity stockQuantity, ProductDimensions dimensions) {
        this.product = product;
        this.price = price;
        addStock(stockQuantity.getValue());
        this.dimensions = dimensions;
    }

    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Quantity to subtract must be greater than 0");
        }

        if (this.stockQuantity.getValue() < quantity) {
            throw new InvalidOperationException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stockQuantity = this.stockQuantity.decrease(quantity);
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Quantity to add must be greater than 0");
        }
        this.stockQuantity = this.stockQuantity.increase(quantity);
    }
}
