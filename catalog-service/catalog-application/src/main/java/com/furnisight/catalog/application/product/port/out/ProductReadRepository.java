package com.furnisight.catalog.application.product.port.out;

import com.furnisight.catalog.application.product.dto.projection.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

public interface ProductReadRepository {
    Optional<ProductDetailProjection> findProductDetailBySlug(String slug);
    Optional<ProductDetailProjection> findProductDetailById(UUID productId);
    SearchProductsProjection searchProducts(SearchProductsQuery query);
    List<RecommendedProductProjection> findRecommendedProducts(
            String categorySlug, String status, int limit);
    List<ProductSummaryProjection> findTopProducts(int limit);
    List<AdminProductProjection> findAdminProducts(String query, String status, String category, int page, int size);
    long countAdminProducts(String query, String status, String category);
    long countProductsByStatus(String status);
    long countLowStockProducts(int threshold);
    long countOutOfStockProducts();
    List<LowStockProductProjection> findLowStockProducts(int threshold, int limit);
}
