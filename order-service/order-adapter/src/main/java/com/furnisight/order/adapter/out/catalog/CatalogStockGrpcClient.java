package com.furnisight.order.adapter.out.catalog;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummaryItem;
import com.furnisight.catalog.ProductSummary;
import com.furnisight.order.application.catalog.port.out.CatalogStockPort;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CatalogStockGrpcClient implements CatalogStockPort {

    @GrpcClient("catalog-service")
    private CatalogServiceGrpc.CatalogServiceBlockingStub catalogStub;

    @Override
    public Map<String, StockItem> getStockItems(Collection<LookupItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }

        com.furnisight.catalog.CheckProductStocksRequest request = com.furnisight.catalog.CheckProductStocksRequest.newBuilder()
                .addAllVariantIds(items.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> item.variantId() != null && !item.variantId().isBlank())
                        .map(LookupItem::variantId)
                        .toList())
                .build();

        Map<String, StockItem> stockItems = new LinkedHashMap<>();
        for (com.furnisight.catalog.ProductStock stock : catalogStub.checkProductStocks(request).getStocksList()) {
            stockItems.put(
                    stockKey(stock.getProductId(), stock.getVariantId()),
                    new StockItem(stock.getProductId(), stock.getVariantId(), stock.getStockQuantity())
            );
        }
        return stockItems;
    }

    private String stockKey(String productId, String variantId) {
        return normalize(productId) + "::" + normalize(variantId);
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
