package com.furnisight.catalog.application.product.port.out;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface FavoriteProductReadRepository {
    List<ProductResponse> findTopFavoritedProductsSince(LocalDateTime since, int limit);
}
