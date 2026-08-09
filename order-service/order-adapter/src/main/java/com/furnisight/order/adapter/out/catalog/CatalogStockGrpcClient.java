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

    @Override
    public Map<String, ProductItem> getProductItems(Collection<LookupItem> items, String locale) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }

        GetProductSummariesRequest request = GetProductSummariesRequest.newBuilder()
                .addAllItems(items.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> item.variantId() != null && !item.variantId().isBlank())
                        .map(item -> GetProductSummaryItem.newBuilder()
                                .setProductId(item.productId())
                                .setSelectedVariantId(item.variantId())
                                .build())
                        .toList())
                .setLocale(locale != null ? locale : "vi")
                .build();

        Map<String, ProductItem> productItems = new LinkedHashMap<>();
        for (ProductSummary product : catalogStub.getProductSummaries(request).getProductsList()) {
            com.furnisight.catalog.ProductSummaryVariant variant = null;
            if (product.hasVariant()) {
                variant = product.getVariant();
            } else if (product.getVariantsCount() > 0) {
                variant = product.getVariants(0);
            }

            productItems.put(
                    stockKey(product.getId(), product.getSelectedVariantId()),
                    new ProductItem(
                            product.getId(),
                            product.getSelectedVariantId(),
                            product.getSlug(),
                            "", // Category name is not in ProductSummary
                            product.getName(),
                            variant != null && variant.hasPrice() ? variant.getPrice() : null,
                            product.getImage(),
                            variant != null && !variant.getColor().isBlank() ? variant.getColor() : null,
                            variant != null && !variant.getMaterial().isBlank() ? variant.getMaterial() : null,
                            variant != null && !variant.getWarranty().isBlank() ? variant.getWarranty() : null,
                            variant != null && variant.hasWeight() ? variant.getWeight() : null,
                            variant != null && variant.hasLength() ? variant.getLength() : null,
                            variant != null && variant.hasWidth() ? variant.getWidth() : null,
                            variant != null && variant.hasHeight() ? variant.getHeight() : null,
                            variant != null && variant.hasStockQuantity() ? variant.getStockQuantity() : null
                    )
            );
        }
        return productItems;
    }
}
