package com.furnisight.promotion.application.port;

import java.util.Collection;
import java.util.Map;

public interface CatalogStockPort {
    Map<String, StockItem> getStockItems(Collection<LookupItem> items);

    record LookupItem(String productId, String variantId) {}
    record StockItem(String productId, String variantId, Integer stockQuantity) {}
}
