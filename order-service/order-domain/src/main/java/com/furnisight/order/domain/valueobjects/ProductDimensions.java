package com.furnisight.order.domain.valueobjects;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDimensions {
    private Double weight; // Weight (gram)
    private Double length; // Length (cm)
    private Double width; // Width (cm)
    private Double height; // Height (cm)

    @Builder
    public ProductDimensions(Double weight, Double length, Double width, Double height) {
        if (weight != null && weight <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS);
        }
        if (length != null && length <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS);
        }
        if (width != null && width <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS);
        }
        if (height != null && height <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS);
        }
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
    }
}
