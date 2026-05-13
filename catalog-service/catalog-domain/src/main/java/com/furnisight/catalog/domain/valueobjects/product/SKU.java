package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.Getter;
import com.furnisight.catalog.domain.exceptions.InvalidSKUException;

import java.util.List;

@Getter
public class SKU extends ValueObject {
    private final String value;

    public SKU(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidSKUException("SKU cannot be null or empty");
        }
        if (value.length() > 50) {
            throw new InvalidSKUException("SKU cannot exceed 50 characters");
        }
        // Basic pattern matching: Alphanumeric and hyphens only
        if (!value.matches("^[a-zA-Z0-9-]+$")) {
            throw new InvalidSKUException("SKU can only contain alphanumeric characters and hyphens");
        }
        this.value = value.trim().toUpperCase();
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
