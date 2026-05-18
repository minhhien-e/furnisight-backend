package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;
import com.furnisight.catalog.domain.exceptions.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDescription extends ValueObject {
    private String value;

    public ProductDescription(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DESCRIPTION, "Product description cannot be null or empty");
        }
        if (value.length() > 3000) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_DESCRIPTION, "Product description cannot exceed 3000 characters");
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
