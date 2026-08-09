package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.enums.ProductStatus;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.ValidationException;
import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import com.furnisight.catalog.domain.valueobjects.product.ProductDescription;
import com.furnisight.catalog.domain.valueobjects.product.ProductName;
import com.furnisight.catalog.domain.valueobjects.product.ProductDimensions;
import com.furnisight.catalog.domain.valueobjects.product.ProductSlug;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends AggregateRoot {
    @Id
    private UUID id;

    private UUID categoryId;

    @Embedded
    private ProductName name;

    @Embedded
    private ProductSlug slug;

    @Column(name = "sku")
    private String sku;

    @Embedded
    private ProductDescription description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "base_price"))
    private Price basePrice;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "jsonb")
    private VariantSpecifications specifications;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;


    @Column(name = "sold_count", nullable = false)
    private Integer soldCount;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private List<String> features;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_media_id")
    private UUID imageMediaId;

    @Column(name = "color")
    private String color;

    @Embedded
    private ProductDimensions dimensions;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    public static Product create(
            UUID categoryId,
            ProductName name,
            ProductSlug slug,
            String sku,
            ProductDescription description,
            List<String> features,
            String imageUrl,
            UUID imageMediaId,
            String color,
            ProductDimensions dimensions,
            List<ProductVariant> variants) {

        if (dimensions == null) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS, "Product dimensions are required");
        }

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .categoryId(categoryId)
                .name(name)
                .slug(slug)
                .sku(sku)
                .description(description)
                .soldCount(0)
                .rating(0.0)
                .ratingCount(0)
                .features(features != null ? features : new ArrayList<>())
                .imageUrl(imageUrl)
                .imageMediaId(imageMediaId)
                .color(color)
                .dimensions(dimensions)
                .variants(new ArrayList<>())
                .productStatus(ProductStatus.ACTIVE)
                .build();

        if (variants != null) {
            variants.forEach(product::addVariant);
        }

        return product;
    }

    public void updateImage(String imageUrl, UUID imageMediaId) {
        this.imageUrl = imageUrl;
        this.imageMediaId = imageMediaId;
    }

    public void addVariant(ProductVariant variant) {
        if (variant == null)
            return;

        if (this.variants == null) {
            this.variants = new ArrayList<>();
        }

        variant.setProduct(this);
        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
            variant.replaceImages(new ArrayList<>(variant.getImages()));
        }

        if (variant.getId() == null) {
            variant.setId(UUID.randomUUID());
            this.variants.add(variant);
            return;
        }

        Optional<ProductVariant> existingVariant = this.variants.stream()
                .filter(v -> variant.getId().equals(v.getId()))
                .findFirst();

        if (existingVariant.isPresent()) {
            ProductVariant existing = existingVariant.get();
            existing.setPrice(variant.getPrice());
            existing.setStockQuantity(variant.getStockQuantity());
            existing.setDimensions(variant.getDimensions());
            existing.setMaterial(variant.getMaterial());
            existing.setWarranty(variant.getWarranty());
            existing.setColor(variant.getColor());
            existing.setSku(variant.getSku());
            existing.setLowStockThreshold(variant.getLowStockThreshold());
            if (variant.getSupports3d() != null || variant.getModelMediaId() != null || variant.getModelUrl() != null) {
                existing.setModelMediaId(variant.getModelMediaId());
                existing.setModelUrl(variant.getModelUrl());
                existing.setSupports3d(variant.getSupports3d() != null ? variant.getSupports3d() : false);
            }
            if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                existing.replaceImages(variant.getImages());
            }
        } else {
            this.variants.add(variant);
        }
    }

    public void removeVariant(UUID variantId) {
        if (this.variants == null || variantId == null)
            return;

        this.variants.removeIf(variant -> variantId.equals(variant.getId()));
    }

    public void activate() {
        this.productStatus = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        this.productStatus = ProductStatus.INACTIVE;
    }

    public void updateProfile(
            ProductName name,
            ProductSlug slug,
            String sku,
            ProductDescription description,
            List<String> features) {
        if (name != null)
            this.name = name;
        if (slug != null)
            this.slug = slug;
        if (sku != null && !sku.isBlank())
            this.sku = sku;
        if (description != null)
            this.description = description;
        if (features != null)
            this.features = features;
    }

    public void changeCategory(UUID categoryId) {
        if (categoryId == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_CATEGORY_NAME,
                    "Category ID cannot be null");
        }
        this.categoryId = categoryId;
    }

    public void updateReviewStats(double newRating, int newRatingCount) {
        this.rating = newRating;
        this.ratingCount = newRatingCount;
    }

    public void incrementSoldCount(int quantity) {
        if (quantity > 0) {
            this.soldCount = (this.soldCount != null ? this.soldCount : 0) + quantity;
        }
    }
}
