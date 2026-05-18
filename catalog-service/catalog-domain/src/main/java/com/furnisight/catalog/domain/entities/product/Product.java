package com.furnisight.catalog.domain.entities.product;

import com.furnisight.catalog.domain.enums.product.ProductStatus;
import com.furnisight.catalog.domain.events.product.ProductCreatedEvent;
import com.furnisight.catalog.domain.events.product.ProductStatusUpdatedEvent;
import com.furnisight.catalog.domain.events.product.ProductUpdatedEvent;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.ForbiddenException;
import com.furnisight.catalog.domain.exceptions.InvalidOperationException;
import com.furnisight.catalog.domain.exceptions.ValidationException;
import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import com.furnisight.catalog.domain.valueobjects.product.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends AggregateRoot {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID shopId;

    private UUID categoryId; // Tham chieu den Category bang ID (DDD: Aggregate reference by ID)

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false, length = 120))
    private ProductName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "description", columnDefinition = "text"))
    private ProductDescription description;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> specs;

    @Column(name = "collection_id")
    private UUID collectionId;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> gallery;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    @Embedded
    private ProductDimensions dimensions;

    @Embedded
    private SeoInfo seoInfo;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class VariantData {
        private SKU sku;
        private Price price;
        private StockQuantity stockQuantity;
    }

    public static Product create(UUID shopId, UUID categoryId, ProductName name, ProductDescription description,
                                 ProductDimensions dimensions, Map<String, Object> attributes, List<VariantData> variantDataList) {
        Product product = Product.builder()
            .shopId(shopId)
            .categoryId(categoryId)
            .name(name)
            .description(description)
            .dimensions(dimensions)
            .attributes(attributes)
            .productStatus(ProductStatus.DRAFT)
            .build();

        if (variantDataList == null || variantDataList.isEmpty()) {
            throw new ValidationException(ErrorCode.MISSING_PRODUCT_VARIANT);
        }
        for (VariantData data : variantDataList) {
            product.addVariants(data.getSku(), data.getPrice(), data.getStockQuantity());
        }

        List<ProductCreatedEvent.ProductVariantDto> variantDtos = new ArrayList<>();
        if (product.getVariants() != null) {
            for (ProductVariant v : product.getVariants()) {
                variantDtos.add(ProductCreatedEvent.ProductVariantDto.builder()
                    .sku(v.getSku().getValue())
                    .price(v.getPrice().getValue().doubleValue())
                    .stockQuantity(v.getStockQuantity().getValue())
                    .build());
            }
        }

        product.registerEvent(ProductCreatedEvent.builder()
            .shopId(product.getShopId())
            .categoryId(product.getCategoryId())
            .productId(product.getId())
            .productName(product.getName().getValue())
            .description(product.getDescription().getValue())
            .weight(product.getDimensions() != null ? product.getDimensions().getWeight() : null)
            .length(product.getDimensions() != null ? product.getDimensions().getLength() : null)
            .height(product.getDimensions() != null ? product.getDimensions().getHeight() : null)
            .width(product.getDimensions() != null ? product.getDimensions().getWidth() : null)
            .attributes(product.getAttributes())
            .variants(variantDtos)
            .occurredAt(LocalDateTime.now())
            .build());

        return product;
    }

    public void activate() {
        if (this.productStatus == ProductStatus.SUSPENDED) {
            throw new InvalidOperationException(ErrorCode.INVALID_PRODUCT_STATE);
        }
        this.productStatus = ProductStatus.ACTIVE;

        registerEvent(ProductStatusUpdatedEvent.builder()
            .shopId(this.shopId)
            .productId(this.id)
            .newStatus(this.productStatus)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    public void updateInfo(ProductName name, ProductDescription description, ProductDimensions dimensions,
                           Map<String, Object> attributes) {
        if (name != null)
            this.name = name;
        if (description != null)
            this.description = description;
        if (dimensions != null)
            this.dimensions = dimensions;
        if (attributes != null)
            this.attributes = attributes;

        registerProductUpdatedEvent();
    }

    public void updateVariants(List<VariantData> variantDataList) {
        if (variantDataList == null || variantDataList.isEmpty()) {
            throw new ValidationException(ErrorCode.MISSING_PRODUCT_VARIANT);
        }

        this.variants.removeIf(existing -> variantDataList.stream()
            .noneMatch(newData -> newData.getSku().equals(existing.getSku())));

        for (VariantData data : variantDataList) {
            Optional<ProductVariant> existingVariant = this.variants.stream()
                .filter(v -> v.getSku().equals(data.getSku())).findFirst();
            if (existingVariant.isPresent()) {
                existingVariant.get().setPrice(data.getPrice());
                existingVariant.get().setStockQuantity(data.getStockQuantity());
            } else {
                this.addVariants(data.getSku(), data.getPrice(), data.getStockQuantity());
            }
        }

        registerProductUpdatedEvent();
    }

    private void registerProductUpdatedEvent() {
        List<ProductUpdatedEvent.ProductVariantDto> variantDtos = this.variants.stream()
            .map(v -> ProductUpdatedEvent.ProductVariantDto.builder()
                .sku(v.getSku().getValue())
                .price(v.getPrice().getValue().doubleValue())
                .stockQuantity(v.getStockQuantity().getValue())
                .build())
            .collect(Collectors.toList());

        registerEvent(ProductUpdatedEvent.builder()
            .shopId(this.shopId)
            .productId(this.id)
            .productName(this.name != null ? this.name.getValue() : null)
            .description(this.description != null ? this.description.getValue() : null)
            .weight(this.dimensions != null ? this.dimensions.getWeight() : null)
            .length(this.dimensions != null ? this.dimensions.getLength() : null)
            .height(this.dimensions != null ? this.dimensions.getHeight() : null)
            .width(this.dimensions != null ? this.dimensions.getWidth() : null)
            .attributes(this.attributes)
            .variants(variantDtos)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    public void verifyOwnership(UUID currentShopId) {
        if (!this.shopId.equals(currentShopId)) {
            throw new ForbiddenException(ErrorCode.PRODUCT_OWNERSHIP_DENIED);
        }
    }

    public void markAsDeleted() {
        this.productStatus = ProductStatus.DELETED;

        registerEvent(ProductStatusUpdatedEvent.builder()
            .shopId(this.shopId)
            .productId(this.id)
            .newStatus(this.productStatus)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    public void addVariants(SKU sku, Price price, StockQuantity stockQuantity) {
        if (this.variants == null) {
            this.variants = new ArrayList<>();
        }

        ProductVariant variant = ProductVariant.builder()
            .product(this)
            .sku(sku)
            .price(price)
            .stockQuantity(stockQuantity)
            .build();
        this.variants.add(variant);
    }

    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }
}
