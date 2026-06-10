package com.furnisight.admin.catalog.product.web.dto.response;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> items,
        int totalPages,
        long totalElements,
        int currentPage
) {
}
