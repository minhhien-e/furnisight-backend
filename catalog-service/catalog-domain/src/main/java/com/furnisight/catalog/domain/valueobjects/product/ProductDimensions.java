package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;
import com.furnisight.catalog.domain.exceptions.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDimensions extends ValueObject {
    private Double weight; // Weight (gram)
    private Double length; // Length (cm)
    private Double width; // Width (cm)
    private Double height; // Height (cm)

    // Validation constructor
    public ProductDimensions(Double weight, Double length, Double width, Double height) {
        if (weight == null || length == null || width == null || height == null) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS, "Dimensions and weight cannot be null");
        }
        if (weight <= 0 || length <= 0 || width <= 0 || height <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DIMENSIONS, "Dimensions and weight must be greater than 0");
        }
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(weight, length, width, height);
    }
}
