package com.furnisight.catalog.presentation.web.rest.dto.response.product;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ProductListResponse {
    private List<ProductItemResponse> products;
    private long total;

    @Getter
    @Builder
    public static class ProductItemResponse {
        private UUID id;
        private String name;
        private String categoryName;
        private Double price;
        private Double oldPrice;
        private String image;
        private String tag;
        private String tagType;
    }
}
