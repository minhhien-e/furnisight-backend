package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.exceptions.InvalidPriceException;
import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class Price extends ValueObject {
    private final BigDecimal value;

    public Price(BigDecimal value) {
        if (value == null) {
            throw new InvalidPriceException("Price cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException("Price cannot be negative");
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
            throw new InvalidPriceException("Quantity for multiplication cannot be negative");
        }
        return new Price(this.value.multiply(BigDecimal.valueOf(quantity)));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
