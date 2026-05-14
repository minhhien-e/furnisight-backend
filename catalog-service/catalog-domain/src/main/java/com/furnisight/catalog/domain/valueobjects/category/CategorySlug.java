package com.furnisight.catalog.domain.valueobjects.category;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;
import com.furnisight.catalog.domain.exceptions.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategorySlug extends ValueObject {
    private String value;

    public CategorySlug(String value){
        if (value == null || value.trim().isEmpty()){
            throw new ValidationException(ErrorCode.INVALID_CATEGORY_SLUG, "Category slug cannot be null or empty");
        }
        String normalized = value.trim().toLowerCase();
        if(!normalized.matches("^[a-z0-9]+(-[a-z0-9]+)*$")){
            throw new ValidationException(ErrorCode.INVALID_CATEGORY_SLUG, "Category slug can only contain lowercase letters, numbers, and hyphens. " +
                "Cannot start or end with a hyphen. Example: 'ao-thun-nam'");
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
