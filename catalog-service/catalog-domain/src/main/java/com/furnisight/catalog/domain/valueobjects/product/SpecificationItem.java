package com.furnisight.catalog.domain.valueobjects.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationItem implements java.io.Serializable {
    private String name;
    private String value;
    private String unit;
}
