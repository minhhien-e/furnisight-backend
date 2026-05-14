package com.furnisight.catalog.application.product.port.out;

import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductReadRepository {
    Optional<ProductDetailProjection> findProductDetailById(UUID productId);
    SearchProductsProjection searchProducts(String query, UUID categoryId, String status, int page, int size);
    List<ProductDetailProjection> findTopProducts(int limit);
}
