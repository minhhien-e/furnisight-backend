package com.furnisight.cart.service;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.GetProductSummaryItem;
import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummariesResponse;
import com.furnisight.catalog.ProductSummary;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class CatalogGrpcClient {

    @GrpcClient("catalog-service")
    private Channel catalogChannel;

    public Map<String, ProductSummary> getProductSummaries(Collection<ProductLookupItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }

        GetProductSummariesRequest request = GetProductSummariesRequest.newBuilder()
                .addAllItems(items.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> item.productId() != null && !item.productId().isBlank())
                        .map(this::toGrpcItem)
                        .toList())
                .build();

        CatalogServiceGrpc.CatalogServiceBlockingStub stub = CatalogServiceGrpc.newBlockingStub(catalogChannel);
        GetProductSummariesResponse response = stub.getProductSummaries(request);

        Map<String, ProductSummary> productMap = new LinkedHashMap<>();
        for (ProductSummary product : response.getProductsList()) {
            productMap.put(keyOf(product.getId(), product.getSelectedVariantId()), product);
        }
        return productMap;
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

    public record ProductLookupItem(String productId, String selectedVariantId) {
    }
}
