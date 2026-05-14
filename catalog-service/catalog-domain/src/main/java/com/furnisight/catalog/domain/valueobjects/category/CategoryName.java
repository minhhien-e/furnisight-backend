package com.furnisight.catalog.domain.valueobjects.category;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;
import com.furnisight.catalog.domain.exceptions.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryName extends ValueObject {
    private String value;

    public CategoryName(String value){
        if(value == null || value.trim().isEmpty()){
            throw new ValidationException(ErrorCode.INVALID_CATEGORY_NAME, "Category name cannot be null or empty");
        }
        if (value.trim().length() < 3 || value.trim().length() > 100){
            throw new ValidationException(ErrorCode.INVALID_CATEGORY_NAME, "Category name must be between 3 and 100 characters");
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
