package com.furnisight.admin.catalog.inventory.application;

import com.furnisight.admin.catalog.LowStockProductDto;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.ProductVariantDto;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.catalog.inventory.web.dto.request.StockInVariantRequest;
import com.furnisight.admin.catalog.inventory.web.dto.response.InventoryItemResponse;
import com.furnisight.admin.catalog.inventory.web.dto.response.InventoryResponse;
import com.furnisight.admin.catalog.inventory.web.dto.response.LowStockItemResponse;
import com.furnisight.admin.catalog.product.application.ProductValidator;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.KpiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final AdminCatalogGrpcClient catalogClient;
    private final ProductValidator validator;

    public List<LowStockItemResponse> getLowStockProducts(int limit, int threshold) {
        return catalogClient.getLowStockProducts(limit, threshold).getProductsList().stream()
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
                new KpiResponse("variants", "Variant", String.valueOf(items.size()), "", "", true, "default", "box"),
                new KpiResponse("stock", "Tổng tồn", String.valueOf(totalStock), "", "", true, "default", "warehouse"),
                new KpiResponse("low", "Sắp hết", String.valueOf(lowStock), "", "", false, "warn", "alert"),
                new KpiResponse("empty", "Hết hàng", String.valueOf(outOfStock), "", "", false, "danger", "ban")
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
                value(variantId), validator.validThreshold(threshold)));
    }

    private InventoryItemResponse toInventoryItem(ProductDto product, ProductVariantDto variant) {
        int stock = variant.getStock();
        int threshold = validator.validThreshold(variant.getLowStockThreshold());
        String status = stock <= 0 ? "cancel" : stock <= threshold ? "low" : "success";
        String statusLabel = stock <= 0 ? "Hết hàng" : stock <= threshold ? "Sắp hết" : "Đủ hàng";
        int stockPercent = Math.min(100, Math.max(0, stock * 100 / 50));
        String label = variant.getLabel().isBlank() ? variant.getId() : variant.getLabel();
        return new InventoryItemResponse(
                product.getId(), variant.getId(),
                variant.getSku().isBlank() ? variant.getId() : variant.getSku(),
                product.getName(), product.getCategory(), label, stock, threshold, stockPercent,
                status, "", "", status, statusLabel);
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
