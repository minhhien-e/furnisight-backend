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
public class SpecificationGroup implements java.io.Serializable {
    private String group;

    @Builder.Default
    private List<SpecificationItem> items = new ArrayList<>();
}
