package com.furnisight.cart.service;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.GetProductSummaryItem;
import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummariesResponse;
import com.furnisight.catalog.ProductSummary;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogGrpcClient {

    @GrpcClient("catalog-service")
    private Channel catalogChannel;

    private final LocalProductCache localProductCache;

    public Map<String, ProductSummary> getProductSummaries(Collection<ProductLookupItem> items, String locale) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }

        String loc = normalizeLocale(locale);
        Map<String, ProductSummary> resultMap = new LinkedHashMap<>();
        List<ProductLookupItem> itemsToFetch = new ArrayList<>();

        for (ProductLookupItem item : items) {
            if (item == null || item.productId() == null || item.productId().isBlank()) continue;
            
            String productKey = keyOf(item.productId(), item.selectedVariantId());
            String cacheKey = productKey + "::" + loc;
            
            ProductSummary cachedProduct = localProductCache.get(cacheKey);
            if (cachedProduct != null) {
                resultMap.put(productKey, cachedProduct);
            } else {
                itemsToFetch.add(item);
            }
        }

        if (itemsToFetch.isEmpty()) {
            return resultMap;
        }

        GetProductSummariesRequest request = GetProductSummariesRequest.newBuilder()
                .addAllItems(itemsToFetch.stream()
                        .map(this::toGrpcItem)
                        .toList())
                .setLocale(loc)
                .build();

        CatalogServiceGrpc.CatalogServiceBlockingStub stub = CatalogServiceGrpc.newBlockingStub(catalogChannel);
        GetProductSummariesResponse response = stub.getProductSummaries(request);

        for (ProductSummary product : response.getProductsList()) {
            String productKey = keyOf(product.getId(), product.getSelectedVariantId());
            resultMap.put(productKey, product);
            localProductCache.put(productKey + "::" + loc, product);
        }
        return resultMap;
    }

    private GetProductSummaryItem toGrpcItem(ProductLookupItem item) {
        return GetProductSummaryItem.newBuilder()
                .setProductId(item.productId())
                .setSelectedVariantId(normalize(item.selectedVariantId()))
                .build();
    }

    public static String keyOf(String productId, String selectedVariantId) {
        return normalize(productId) + "::" + normalize(selectedVariantId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    private static String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "vi";
        }
        String normalized = locale.trim().toLowerCase();
        return normalized.startsWith("en") ? "en" : "vi";
    }

    public record ProductLookupItem(String productId, String selectedVariantId) {
    }

}
