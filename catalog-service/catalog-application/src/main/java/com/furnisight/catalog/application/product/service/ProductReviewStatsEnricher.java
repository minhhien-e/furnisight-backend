package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.dto.response.ProductReviewStats;
import com.furnisight.catalog.application.product.port.out.ProductReviewStatsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductReviewStatsEnricher {

    private final ProductReviewStatsPort productReviewStatsPort;

    public ProductResponse enrich(ProductResponse product) {
        if (product == null || product.getId() == null) {
            return product;
        }
        applyStats(List.of(product));
        return product;
    }

    public List<ProductResponse> enrichAll(List<ProductResponse> products) {
        applyStats(products);
        return products;
    }

    private void applyStats(List<ProductResponse> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        Collection<UUID> productIds = products.stream()
                .map(ProductResponse::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<UUID, ProductReviewStats> statsMap = productReviewStatsPort.getProductReviewStats(productIds);
        for (ProductResponse product : products) {
            ProductReviewStats stats = statsMap.get(product.getId());
            if (stats == null) {
                product.setRating(0D);
                product.setRatingCount(0);
                continue;
            }
            product.setRating(stats.rating());
            product.setRatingCount(stats.ratingCount());
        }
    }
}
