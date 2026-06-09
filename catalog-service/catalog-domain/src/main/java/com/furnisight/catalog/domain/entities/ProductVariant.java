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

    /** Chất liệu chính — required (TEXT) */
    @Column(name = "material", nullable = false)
    private String material;

    /** Thông tin bảo hành — optional (TEXT), ví dụ: "12 tháng", "2 năm kết cấu" */
    @Column(name = "warranty")
    private String warranty;

    @Column(name = "color")
    private String color;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 5;

    public ProductVariant(Product product, Price price, StockQuantity stockQuantity,
                          ProductDimensions dimensions, String material, String warranty,
                          String color) {
        this.id = UUID.randomUUID();
        this.stockQuantity = new StockQuantity(0);
        update(product, price, stockQuantity, dimensions, material, warranty, color);
    }

    public void update(Product product, Price price, StockQuantity stockQuantity,
                       ProductDimensions dimensions, String material, String warranty,
                       String color) {
        if (material == null || material.isBlank()) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS, "Material cannot be blank");
        }
        this.product = product;
        this.price = price;
        addStock(stockQuantity.getValue());
        this.dimensions = dimensions;
        this.material = material;
        this.warranty = warranty;
        this.color = color;
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
