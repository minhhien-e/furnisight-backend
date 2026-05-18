package com.furnisight.catalog.domain.valueobjects.product;

import com.furnisight.catalog.domain.seedwork.ValueObject;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeoInfo extends ValueObject {
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    @Override
    protected List<Object> getEqualityComponents() {
        return Arrays.asList(metaTitle, metaDescription, metaKeywords);
    }
}
