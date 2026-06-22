package com.furnisight.promotion.adapter.out.integration;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummaryItem;
import com.furnisight.catalog.ProductSummary;
import com.furnisight.promotion.application.port.CatalogStockPort;
import io.grpc.Channel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CatalogStockGrpcClient implements CatalogStockPort {
    @GrpcClient("catalog-service")
    private Channel catalogChannel;

    @Override
    public Map<String, StockItem> getStockItems(Collection<LookupItem> items) {
        if (items == null || items.isEmpty()) return Map.of();
        var request = GetProductSummariesRequest.newBuilder()
                .addAllItems(items.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> item.productId() != null && !item.productId().isBlank())
                        .map(item -> GetProductSummaryItem.newBuilder()
                                .setProductId(item.productId())
                                .setSelectedVariantId(normalize(item.variantId()))
                                .build())
                        .toList())
                .build();
        Map<String, StockItem> result = new LinkedHashMap<>();
        for (ProductSummary product : CatalogServiceGrpc.newBlockingStub(catalogChannel)
                .getProductSummaries(request).getProductsList()) {
            Integer stock = product.hasVariant() && product.getVariant().hasStockQuantity()
                    ? product.getVariant().getStockQuantity() : null;
            result.put(key(product.getId(), product.getSelectedVariantId()),
                    new StockItem(product.getId(), product.getSelectedVariantId(), stock));
        }
        return result;
    }

    public static String key(String productId, String variantId) {
        return normalize(productId) + "::" + normalize(variantId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
