package com.furnisight.admin.catalog.inventory.application;

import com.furnisight.admin.catalog.LowStockProductDto;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.ProductVariantDto;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.catalog.inventory.web.dto.request.StockInVariantRequest;
import com.furnisight.admin.catalog.inventory.web.dto.response.InventoryItemResponse;
import com.furnisight.admin.catalog.inventory.web.dto.response.InventoryResponse;
import com.furnisight.admin.catalog.inventory.web.dto.response.LowStockItemResponse;

import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.KpiResponse;
import com.furnisight.admin.shared.web.KpiType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final AdminCatalogGrpcClient catalogClient;

    public List<LowStockItemResponse> getLowStockProducts(int limit) {
        return catalogClient.getLowStockProducts(limit).getProductsList().stream()
                .map(this::toLowStockResponse)
                .toList();
    }

    public InventoryResponse getInventory(String query) {
        com.furnisight.admin.catalog.ProductPageResponse response =
                catalogClient.getProducts(1, 500, query, null, null);
        List<InventoryItemResponse> items = response.getProductsList().stream()
                .flatMap(product -> product.getVariantsList().stream()
                        .map(variant -> toInventoryItem(product, variant)))
                .toList();
        long totalStock = items.stream().mapToLong(InventoryItemResponse::stock).sum();
        long lowStock = items.stream().filter(item -> item.stock() > 0 && item.stock() <= item.threshold()).count();
        long outOfStock = items.stream().filter(item -> item.stock() <= 0).count();
        return new InventoryResponse(List.of(
                new KpiResponse(KpiType.VARIANTS, items.size(), 0D),
                new KpiResponse(KpiType.STOCK, totalStock, 0D),
                new KpiResponse(KpiType.LOW_STOCK, lowStock, 0D),
                new KpiResponse(KpiType.OUT_OF_STOCK, outOfStock, 0D)
        ), items);
    }

    public ActionResultResponse stockInVariant(StockInVariantRequest request) {
        return toActionResult(catalogClient.stockInVariant(
                com.furnisight.admin.catalog.StockInVariantRequest.newBuilder()
                        .setProductId(value(request.productId()))
                        .setVariantId(value(request.variantId()))
                        .setQuantity(request.quantity())
                        .setNote(value(request.note()))
                        .build()));
    }

    public ActionResultResponse updateVariantThreshold(String variantId, int threshold) {
        return toActionResult(catalogClient.updateVariantThreshold(
                value(variantId), threshold));
    }

    private InventoryItemResponse toInventoryItem(ProductDto product, ProductVariantDto variant) {
        return new InventoryItemResponse(
                product.getId(), variant.getId(),
                variant.getSku(),
                product.getName(), product.getCategory(), variant.getLabel(),
                variant.getStock(), variant.getLowStockThreshold(), "", "");
    }

    private LowStockItemResponse toLowStockResponse(LowStockProductDto product) {
        return new LowStockItemResponse(
                product.getName(), product.getCategory(), product.getStock(), product.getLevel());
    }

    private ActionResultResponse toActionResult(com.furnisight.admin.catalog.AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
