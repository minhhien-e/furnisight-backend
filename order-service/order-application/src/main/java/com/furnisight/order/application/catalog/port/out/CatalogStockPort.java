package com.furnisight.order.application.catalog.port.out;

import java.util.Collection;
import java.util.Map;

public interface CatalogStockPort {
    Map<String, StockItem> getStockItems(Collection<LookupItem> items);

    record LookupItem(String productId, String variantId) {
    }

    record StockItem(String productId, String variantId, Integer stockQuantity) {
    }

    record ProductItem(
            String productId,
            String variantId,
            String slug,
            String categoryName,
            String productName,
            Double price,
            String imageUrl,
            String color,
            String material,
            String warranty,
            Double weight,
            Double length,
            Double width,
            Double height,
            Integer stockQuantity
    ) {
    }

    Map<String, ProductItem> getProductItems(Collection<LookupItem> items, String locale);
}
