package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.Getter;
import com.furnisight.catalog.domain.exceptions.InsufficientStockException;
import com.furnisight.catalog.domain.exceptions.InvalidStockQuantityException;

import java.util.List;

@Getter
public class StockQuantity extends ValueObject {
    private final Integer value;

    public StockQuantity(Integer value) {
        if (value == null) {
            throw new InvalidStockQuantityException("Stock quantity cannot be null");
        }
        if (value < 0) {
            throw new InvalidStockQuantityException("Stock quantity cannot be negative");
        }
        this.value = value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    public StockQuantity decrease(int quantityToDecrease) {
        if (quantityToDecrease < 0) {
            throw new InvalidStockQuantityException("Quantity to decrease cannot be negative");
        }
        if (this.value - quantityToDecrease < 0) {
            throw new InsufficientStockException("Unknown SKU", this.value, quantityToDecrease);
            // InsufficientStockException
            // takes message and params
        }
        return new StockQuantity(this.value - quantityToDecrease);
    }

    public StockQuantity increase(int quantityToIncrease) {
        if (quantityToIncrease < 0) {
            throw new InvalidStockQuantityException("Quantity to increase cannot be negative");
        }
        return new StockQuantity(this.value + quantityToIncrease);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
