package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import java.util.UUID;

public interface SearchProductsUseCase {
    SearchProductsProjection execute(String query, UUID categoryId, String status, int page, int size);
}
