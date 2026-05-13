package com.furnisight.catalog.domain.valueobjects.category;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import com.furnisight.catalog.domain.exceptions.InvalidCategorySlugException;
import lombok.Getter;

import java.util.List;

@Getter
public class CategorySlug extends ValueObject {
    private final String value;

    public CategorySlug(String value){
        if (value == null || value.trim().isEmpty()){
            throw new InvalidCategorySlugException("Category slug cannot be null or empty");
        }
        String normalized = value.trim().toLowerCase();
        if(!normalized.matches("^[a-z0-9]+(-[a-z0-9]+)*$")){
            throw new InvalidCategorySlugException("Category slug can only contain lowercase letters, numbers, and hyphens. " +
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
