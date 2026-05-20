package com.furnisight.catalog.application.product.port.out;

import com.furnisight.catalog.application.product.dto.projection.ProductSummaryProjection;
import java.time.LocalDateTime;
import java.util.List;

public interface FavoriteProductReadRepository {
    List<ProductSummaryProjection> findTopFavoritedProductsSince(LocalDateTime since, int limit);
}
