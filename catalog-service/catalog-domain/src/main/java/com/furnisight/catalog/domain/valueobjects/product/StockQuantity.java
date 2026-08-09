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
public class StockQuantity extends ValueObject {
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer value;

    public StockQuantity(Integer value) {
        if (value == null) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Stock quantity cannot be null");
        }
        if (value < 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Stock quantity cannot be negative");
        }
        this.value = value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    public StockQuantity decrease(int quantityToDecrease) {
        if (quantityToDecrease < 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Quantity to decrease cannot be negative");
        }
        if (this.value - quantityToDecrease < 0) {
            throw new InvalidOperationException(ErrorCode.INSUFFICIENT_STOCK);
        }
        return new StockQuantity(this.value - quantityToDecrease);
    }

    public StockQuantity increase(int quantityToIncrease) {
        if (quantityToIncrease < 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_QUANTITY, "Quantity to increase cannot be negative");
        }
        return new StockQuantity(this.value + quantityToIncrease);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
