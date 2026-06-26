package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.enums.ProductStatus;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.ValidationException;
import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import com.furnisight.catalog.domain.valueobjects.product.ProductDescription;
import com.furnisight.catalog.domain.valueobjects.product.ProductName;
import com.furnisight.catalog.domain.valueobjects.product.ProductSlug;
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

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;


    @Column(name = "sold_count", nullable = false)
    private Integer soldCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private List<String> features;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> gallery;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    public static Product create(
            UUID categoryId,
            ProductName name,
            ProductSlug slug,
            String sku,
            ProductDescription description,
            List<String> features,
            List<ProductImage> gallery,
            List<ProductVariant> variants) {

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .categoryId(categoryId)
                .name(name)
                .slug(slug)
                .sku(sku)
                .description(description)
                .soldCount(0)
                .features(features != null ? features : new ArrayList<>())
                .gallery(new ArrayList<>())
                .variants(new ArrayList<>())
                .productStatus(ProductStatus.ACTIVE)
                .build();

        if (gallery != null) {
            gallery.forEach(product::addImage);
        }

        if (variants != null) {
            variants.forEach(product::addVariant);
        }

        return product;
    }

    public void addImage(ProductImage image) {
        if (image == null)
            return;

        if (this.gallery == null) {
            this.gallery = new ArrayList<>();
        }

        image.setProduct(this);
        this.gallery.add(image);
    }

    public void moveImage(ProductImage image, int newPosition) {
        if (image == null)
            return;
        if (this.gallery == null || this.gallery.isEmpty())
            return;

        if (newPosition < 0 || newPosition >= this.gallery.size()) {
            return;
        }

        ProductImage targetImage = this.gallery.stream()
                .filter(i -> Objects.equals(i.getPosition(), newPosition))
                .findFirst()
                .orElse(null);

        if (targetImage == null)
            return;

        Integer currentPosition = image.getPosition();

        image.setPosition(newPosition);
        targetImage.setPosition(currentPosition);

        this.gallery.sort(Comparator.comparing(ProductImage::getPosition));
    }

    public void removeImage(UUID imageId) {
        if (this.gallery == null || imageId == null)
            return;

        this.gallery.removeIf(image -> imageId.equals(image.getId()));
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

}
