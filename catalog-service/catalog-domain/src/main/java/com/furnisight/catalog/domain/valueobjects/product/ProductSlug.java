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
public class ProductSlug extends ValueObject {
    
    @Column(name = "slug", nullable = false, unique = true)
    private String value;

    public ProductSlug(String value){
        if (value == null || value.trim().isEmpty()){
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_SLUG, "Product slug cannot be null or empty");
        }
        String normalized = value.trim().toLowerCase();
        if(!normalized.matches("^[a-z0-9]+(-[a-z0-9]+)*$")){
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_SLUG, "Product slug can only contain lowercase letters, numbers, and hyphens.");
        }
        this.value = normalized;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    @Override
    public String toString(){
        return this.value;
    }
}
