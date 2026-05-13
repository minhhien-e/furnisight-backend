package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.Getter;
import com.furnisight.catalog.domain.exceptions.InvalidProductDescriptionException;

import java.util.List;

@Getter
public class ProductDescription extends ValueObject {
    private final String value;

    public ProductDescription(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidProductDescriptionException("Product description cannot be null or empty");
        }
        if (value.length() > 3000) {
            throw new InvalidProductDescriptionException("Product description cannot exceed 3000 characters");
        }
        this.value = value.trim();
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
