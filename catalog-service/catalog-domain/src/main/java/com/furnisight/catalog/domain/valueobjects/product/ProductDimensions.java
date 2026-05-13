package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.Builder;
import lombok.Getter;
import com.furnisight.catalog.domain.exceptions.InvalidProductDimensionsException;

import java.util.List;

@Getter
@Builder
public class ProductDimensions extends ValueObject {
    private final Double weight; // Weight (gram)
    private final Double length; // Length (cm)
    private final Double width; // Width (cm)
    private final Double height; // Height (cm)

    // Validation constructor
    public ProductDimensions(Double weight, Double length, Double width, Double height) {
        if (weight == null || length == null || width == null || height == null) {
            throw new InvalidProductDimensionsException("Dimensions and weight cannot be null");
        }
        if (weight <= 0 || length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidProductDimensionsException("Dimensions and weight must be greater than 0");
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
