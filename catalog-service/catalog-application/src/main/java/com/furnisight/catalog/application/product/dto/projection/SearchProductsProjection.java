package com.furnisight.catalog.application.product.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchProductsProjection {
    private List<ProductDetailProjection> products;
    private long total;
}
