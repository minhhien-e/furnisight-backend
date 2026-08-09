package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

public interface SearchProductsUseCase {
    PageResponse<ProductResponse> execute(SearchProductsQuery query);
}
