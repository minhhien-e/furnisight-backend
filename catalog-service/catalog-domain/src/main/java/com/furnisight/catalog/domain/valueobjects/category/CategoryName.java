package com.furnisight.catalog.domain.valueobjects.category;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import com.furnisight.catalog.domain.exceptions.InvalidCategoryNameException;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryName extends ValueObject {
    private final String value;

    public CategoryName(String value){
        if(value == null || value.trim().isEmpty()){
            throw new InvalidCategoryNameException("Category name cannot be null or empty");
        }
        if (value.trim().length() < 3 || value.trim().length() > 100){
            throw new InvalidCategoryNameException("Category name must be between 3 and 100 characters");
        }
        this.value = value.trim();
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
