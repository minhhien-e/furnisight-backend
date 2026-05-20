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
    private List<ProductSummaryProjection> products;
    private long total;
    private int page;
    private int pageSize;
    private Facets facets;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Facets {
        private List<CategoryFacet> categories;
        private List<MaterialFacet> materials;
        private List<ColorFacet> colors;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryFacet {
        private String id;    // UUID
        private String slug;  // e.g. "sofa", "dining-table"
        private String label;
        private long count;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialFacet {
        private String id;
        private String label;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColorFacet {
        private String id;
        private String label;
        private String hex;
    }
}
