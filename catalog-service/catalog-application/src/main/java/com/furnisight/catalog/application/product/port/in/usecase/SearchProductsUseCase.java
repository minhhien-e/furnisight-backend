package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

public interface SearchProductsUseCase {
    SearchProductsProjection execute(SearchProductsQuery query);
}
