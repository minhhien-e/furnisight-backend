package com.furnisight.catalog.application.product.port.out;

import com.furnisight.catalog.application.product.dto.response.ProductReviewStats;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductReviewStatsPort {

    Map<UUID, ProductReviewStats> getProductReviewStats(Collection<UUID> productIds);
    List<ProductResponse.Review> getProductReviews(UUID productId, int limit);
}
