package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;
import com.furnisight.catalog.domain.exceptions.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SKU extends ValueObject {
    private String value;

    public SKU(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_SKU, "SKU cannot be null or empty");
        }
        if (value.length() > 50) {
            throw new ValidationException(ErrorCode.INVALID_SKU, "SKU cannot exceed 50 characters");
        }
        // Basic pattern matching: Alphanumeric and hyphens only
        if (!value.matches("^[a-zA-Z0-9-]+$")) {
            throw new ValidationException(ErrorCode.INVALID_SKU, "SKU can only contain alphanumeric characters and hyphens");
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
