package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;
import com.furnisight.catalog.domain.exceptions.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.List;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductName extends ValueObject {

    @Column(name = "name", nullable = false, length = 120)
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
