package com.furnisight.catalog.application.product.port.out;

import com.furnisight.catalog.application.product.dto.projection.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

public interface ProductReadRepository {
    Optional<ProductDetailProjection> findProductDetailById(UUID productId);
    SearchProductsProjection searchProducts(SearchProductsQuery query);
    List<ProductDetailProjection> findTopProducts(int limit);
    Optional<ProductEsProjection> findProductDocumentById(UUID productId);
}

