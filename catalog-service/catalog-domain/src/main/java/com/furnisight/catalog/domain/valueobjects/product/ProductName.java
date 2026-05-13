package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.Getter;
import com.furnisight.catalog.domain.exceptions.InvalidProductNameException;

import java.util.List;

@Getter
public class ProductName extends ValueObject {
    private final String value;

    public ProductName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidProductNameException("Product name cannot be null or empty");
        }
        if (value.length() < 10 || value.length() > 120) {
            throw new InvalidProductNameException("Product name must be between 10 and 120 characters");
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
