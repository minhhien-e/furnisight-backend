package com.furnisight.catalog.domain.valueobjects.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantSpecifications implements java.io.Serializable {
    @Builder.Default
    private List<SpecificationGroup> specGroups = new ArrayList<>();
}
