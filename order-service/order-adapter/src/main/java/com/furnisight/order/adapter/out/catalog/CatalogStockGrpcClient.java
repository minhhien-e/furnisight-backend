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

        GetProductSummariesRequest request = GetProductSummariesRequest.newBuilder()
                .addAllItems(items.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> item.productId() != null && !item.productId().isBlank())
                        .map(this::toGrpcItem)
                        .toList())
                .build();

        Map<String, StockItem> stockItems = new LinkedHashMap<>();
        for (ProductSummary product : catalogStub.getProductSummaries(request).getProductsList()) {
            Integer stockQuantity = product.hasVariant() && product.getVariant().hasStockQuantity()
                    ? product.getVariant().getStockQuantity()
                    : null;
            stockItems.put(
                    stockKey(product.getId(), product.getSelectedVariantId()),
                    new StockItem(product.getId(), product.getSelectedVariantId(), stockQuantity)
            );
        }
        return stockItems;
    }

    private GetProductSummaryItem toGrpcItem(LookupItem item) {
        return GetProductSummaryItem.newBuilder()
                .setProductId(item.productId())
                .setSelectedVariantId(normalize(item.variantId()))
                .build();
    }

    private String stockKey(String productId, String variantId) {
        return normalize(productId) + "::" + normalize(variantId);
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
