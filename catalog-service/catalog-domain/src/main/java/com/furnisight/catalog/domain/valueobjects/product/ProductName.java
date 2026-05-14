package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;
import com.furnisight.catalog.domain.exceptions.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductName extends ValueObject {
    private String value;

    public ProductName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_NAME, "Product name cannot be null or empty");
        }
        if (value.length() < 10 || value.length() > 120) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_NAME, "Product name must be between 10 and 120 characters");
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
