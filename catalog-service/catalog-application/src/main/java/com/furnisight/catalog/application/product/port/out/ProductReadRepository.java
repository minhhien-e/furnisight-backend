package com.furnisight.catalog.application.product.port.out;

import com.furnisight.catalog.application.product.dto.response.*;
import com.furnisight.catalog.application.common.dto.PageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

public interface ProductReadRepository {
    Optional<ProductResponse> findProductDetailById(UUID productId);
    List<ProductResponse> findProductDetailsByIds(List<UUID> productIds);
    Optional<ProductResponse> findProductDetailBySlug(String slug);
    List<ProductResponse.ProductStockDto> findStockByVariantIds(List<UUID> variantIds);
    PageResponse<ProductResponse> searchProducts(SearchProductsQuery queryParam);
    List<ProductResponse> findRecommendedProducts(
            String categorySlug, String status, int limit);
    List<ProductResponse> findTopProducts(int limit);
    List<ProductResponse> findAdminProducts(String query, String status, String category, int page, int size);
    long countAdminProducts(String query, String status, String category);
    long countProductsByStatus(String status);
    long countLowStockProducts();
    long countOutOfStockProducts();
    List<ProductResponse> findLowStockProducts(int limit);
}
