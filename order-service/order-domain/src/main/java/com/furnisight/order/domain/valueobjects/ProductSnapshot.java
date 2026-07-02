package com.furnisight.order.domain.valueobjects;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSnapshot {
    private String productId;
    private String variantId;
    private String slug;
    
    // Product details
    private String categoryName;
    private String productName;
    // Variant details
    private String color;
    private String material;
    private String warranty;
    @Embedded
    private ProductDimensions dimensions;

    // Display
    private String imageUrl;

    @Builder
    public ProductSnapshot(String productId, String variantId, String slug, String categoryName, 
                           String productName, String color, String material, 
                           String warranty, ProductDimensions dimensions, String imageUrl) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.PRODUCT_ID_EMPTY);
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_INFO);
        }
        this.productId = productId;
        this.variantId = variantId;
        this.slug = slug;
        this.categoryName = categoryName;
        this.productName = productName;
        this.color = color;
        this.material = material;
        this.warranty = warranty;
        this.dimensions = dimensions;
        this.imageUrl = imageUrl;
    }
}
