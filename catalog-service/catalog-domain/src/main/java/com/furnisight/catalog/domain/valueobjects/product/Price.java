package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import com.furnisight.catalog.domain.exceptions.*;
import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Price extends ValueObject {

    @Column(name = "price", nullable = false)
    private BigDecimal value;

    public Price(BigDecimal value) {
        if (value == null) {
            throw new ValidationException(ErrorCode.INVALID_PRICE, "Price cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVALID_PRICE, "Price cannot be negative");
        }
        this.value = value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    public Price add(Price other) {
        return new Price(this.value.add(other.getValue()));
    }

    public Price subtract(Price other) {
        return new Price(this.value.subtract(other.getValue()));
    }

    public Price multiply(int quantity) {
        if (quantity < 0) {
            throw new ValidationException(ErrorCode.INVALID_PRICE, "Quantity for multiplication cannot be negative");
        }
        return new Price(this.value.multiply(BigDecimal.valueOf(quantity)));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
