package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class SeoInfo extends ValueObject {
    private final String metaTitle;
    private final String metaDescription;
    private final String metaKeywords;

    @Override
    protected List<Object> getEqualityComponents() {
        return Arrays.asList(metaTitle, metaDescription, metaKeywords);
    }
}
