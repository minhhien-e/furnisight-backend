package com.furnisight.admin.service;

import com.furnisight.admin.catalog.CategoryDto;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.CreateCategoryRequest;
import com.furnisight.admin.catalog.CreateProductRequest;
import com.furnisight.admin.catalog.LowStockProductDto;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.ProductPageResponse;
import com.furnisight.admin.catalog.ProductVariantDto;
import com.furnisight.admin.catalog.ProductVariantInput;
import com.furnisight.admin.catalog.UpdateCategoryRequest;
import com.furnisight.admin.catalog.UpdateProductRequest;
import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminCategoryListResponse;
import com.furnisight.admin.controller.dto.AdminCategoryResponse;
import com.furnisight.admin.controller.dto.AdminInventoryItemResponse;
import com.furnisight.admin.controller.dto.AdminInventoryResponse;
import com.furnisight.admin.controller.dto.AdminProductPageResponse;
import com.furnisight.admin.controller.dto.AdminProductResponse;
import com.furnisight.admin.controller.dto.AdminProductVariantResponse;
import com.furnisight.admin.controller.dto.DashboardLowStockResponse;
import com.furnisight.admin.controller.dto.DashboardKpiResponse;
import com.furnisight.admin.controller.dto.SaveAdminCategoryRequest;
import com.furnisight.admin.controller.dto.SaveAdminProductRequest;
import com.furnisight.admin.controller.dto.SaveAdminProductVariantRequest;
import com.furnisight.admin.controller.dto.StockInVariantRequest;
import com.furnisight.admin.integration.GrpcAdminCatalogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final GrpcAdminCatalogClient grpcAdminCatalogClient;

    public AdminProductPageResponse getProducts(int page, int size, String query, String status, String category) {
        ProductPageResponse response = grpcAdminCatalogClient.getProducts(page, size, query, status, category);
        return new AdminProductPageResponse(
                response.getProductsList().stream().map(this::toProductResponse).toList(),
                response.getTotalPages(),
                response.getTotalElements(),
                response.getCurrentPage());
    }

    public AdminActionResultResponse createProduct(SaveAdminProductRequest request) {
        validateVariants(request.variants());
        return toActionResult(grpcAdminCatalogClient.createProduct(CreateProductRequest.newBuilder()
                .setName(value(request.name()))
                .setSlug(slugFrom(request.sku(), request.name()))
                .setCategory(value(request.category()))
                .setPrice(request.price())
                .setStock(request.stock())
                .setSku(value(request.sku()))
                .setStatus(productStatusInput(request))
                .setDescription(value(request.description()))
                .setModelMediaId(value(request.modelMediaId()))
                .setModelUrl(value(request.modelUrl()))
                .setSupports3D(request.supports3d())
                .addAllImageUrls(cleanList(request.imageUrls()))
                .addAllVariants(toVariantInputs(request.variants()))
                .build()));
    }

    public AdminActionResultResponse updateProduct(String id, SaveAdminProductRequest request) {
        validateVariants(request.variants());
        return toActionResult(grpcAdminCatalogClient.updateProduct(UpdateProductRequest.newBuilder()
                .setId(value(id))
                .setName(value(request.name()))
                .setSlug(slugFrom(request.sku(), request.name()))
                .setCategory(value(request.category()))
                .setPrice(request.price())
                .setStock(request.stock())
                .setSku(value(request.sku()))
                .setStatus(productStatusInput(request))
                .setDescription(value(request.description()))
                .setModelMediaId(value(request.modelMediaId()))
                .setModelUrl(value(request.modelUrl()))
                .setSupports3D(request.supports3d())
                .addAllImageUrls(cleanList(request.imageUrls()))
                .addAllVariants(toVariantInputs(request.variants()))
                .build()));
    }

    public AdminProductResponse getProduct(String id) {
        return toProductResponse(grpcAdminCatalogClient.getProductDetail(id));
    }

    public AdminActionResultResponse deleteProduct(String id) {
        return toActionResult(grpcAdminCatalogClient.deleteProduct(id));
    }

    public AdminCategoryListResponse getCategories(String query) {
        CategoryListResponse response = grpcAdminCatalogClient.getCategories(query);
        return new AdminCategoryListResponse(response.getCategoriesList().stream()
                .map(this::toCategoryResponse)
                .toList());
    }

    public AdminActionResultResponse createCategory(SaveAdminCategoryRequest request) {
        return toActionResult(grpcAdminCatalogClient.createCategory(CreateCategoryRequest.newBuilder()
                .setName(value(request.name()))
                .setSlug(value(request.slug()))
                .setIconId(value(request.iconId()))
                .setVisible(request.visible())
                .setDescription(value(request.description()))
                .setImageUrl(value(request.imageUrl()))
                .build()));
    }

    public AdminActionResultResponse updateCategory(String id, SaveAdminCategoryRequest request) {
        return toActionResult(grpcAdminCatalogClient.updateCategory(UpdateCategoryRequest.newBuilder()
                .setId(value(id))
                .setName(value(request.name()))
                .setSlug(value(request.slug()))
                .setIconId(value(request.iconId()))
                .setVisible(request.visible())
                .setDescription(value(request.description()))
                .setImageUrl(value(request.imageUrl()))
                .build()));
    }

    public AdminActionResultResponse deleteCategory(String id) {
        return toActionResult(grpcAdminCatalogClient.deleteCategory(id));
    }

    public List<DashboardLowStockResponse> getLowStockProducts(int limit, int threshold) {
        return grpcAdminCatalogClient.getLowStockProducts(limit, threshold).getProductsList().stream()
                .map(this::toDashboardLowStock)
                .toList();
    }

    public AdminInventoryResponse getInventory(String query) {
        ProductPageResponse response = grpcAdminCatalogClient.getProducts(1, 500, query, null, null);
        List<AdminInventoryItemResponse> items = response.getProductsList().stream()
                .flatMap(product -> product.getVariantsList().stream().map(variant -> toInventoryItem(product, variant)))
                .toList();
        long totalStock = items.stream().mapToLong(AdminInventoryItemResponse::stock).sum();
        long lowStock = items.stream().filter(item -> item.stock() > 0 && item.stock() <= item.threshold()).count();
        long outOfStock = items.stream().filter(item -> item.stock() <= 0).count();
        return new AdminInventoryResponse(List.of(
                new DashboardKpiResponse("variants", "Variant", String.valueOf(items.size()), "", "", true, "default", "box"),
                new DashboardKpiResponse("stock", "Tổng tồn", String.valueOf(totalStock), "", "", true, "default", "warehouse"),
                new DashboardKpiResponse("low", "Sắp hết", String.valueOf(lowStock), "", "", false, "warn", "alert"),
                new DashboardKpiResponse("empty", "Hết hàng", String.valueOf(outOfStock), "", "", false, "danger", "ban")
        ), items);
    }

    public AdminActionResultResponse stockInVariant(StockInVariantRequest request) {
        return toActionResult(grpcAdminCatalogClient.stockInVariant(com.furnisight.admin.catalog.StockInVariantRequest.newBuilder()
                .setProductId(value(request.productId()))
                .setVariantId(value(request.variantId()))
                .setQuantity(request.quantity())
                .setNote(value(request.note()))
                .build()));
    }

    private AdminProductResponse toProductResponse(ProductDto product) {
        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getStatusLabel(),
                product.getModelMediaId(),
                product.getModelUrl(),
                product.getSupports3D(),
                product.getModel3DFileName(),
                product.getModel3DSize(),
                product.getImageUrlsList(),
                product.getVariantsList().stream().map(this::toVariantResponse).toList());
    }

    private AdminProductVariantResponse toVariantResponse(ProductVariantDto variant) {
        return new AdminProductVariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getPrice(),
                variant.getStock(),
                variant.getColor(),
                variant.getMaterial(),
                variant.getWarranty(),
                variant.getWeight(),
                variant.getLength(),
                variant.getWidth(),
                variant.getHeight(),
                variant.getLabel(),
                variant.getLowStockThreshold());
    }

    private ProductVariantInput toVariantInput(SaveAdminProductVariantRequest variant) {
        return ProductVariantInput.newBuilder()
                .setId(value(variant.id()))
                .setSku(normalizeSku(variant.sku()))
                .setPrice(variant.price())
                .setStock(variant.stock())
                .setColor(value(variant.color()))
                .setMaterial(value(variant.material()))
                .setWarranty(value(variant.warranty()))
                .setWeight(variant.weight())
                .setLength(variant.length())
                .setWidth(variant.width())
                .setHeight(variant.height())
                .setLowStockThreshold(validThreshold(variant.lowStockThreshold()))
                .build();
    }

    private List<ProductVariantInput> toVariantInputs(List<SaveAdminProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            return List.of();
        }
        return variants.stream().map(this::toVariantInput).toList();
    }

    private AdminInventoryItemResponse toInventoryItem(ProductDto product, ProductVariantDto variant) {
        int stock = variant.getStock();
        int threshold = validThreshold(variant.getLowStockThreshold());
        String status = stock <= 0 ? "cancel" : stock <= threshold ? "low" : "success";
        String statusLabel = stock <= 0 ? "Hết hàng" : stock <= threshold ? "Sắp hết" : "Đủ hàng";
        int stockPercent = Math.min(100, Math.max(0, stock * 100 / 50));
        String label = variant.getLabel().isBlank() ? variant.getId() : variant.getLabel();
        return new AdminInventoryItemResponse(
                product.getId(),
                variant.getId(),
                variant.getSku().isBlank() ? variant.getId() : variant.getSku(),
                product.getName(),
                product.getCategory(),
                label,
                stock,
                threshold,
                stockPercent,
                status,
                "",
                "",
                status,
                statusLabel);
    }

    public AdminActionResultResponse updateVariantThreshold(String variantId, int threshold) {
        return toActionResult(grpcAdminCatalogClient.updateVariantThreshold(
                value(variantId), validThreshold(threshold)));
    }

    private void validateVariants(List<SaveAdminProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm phải có ít nhất một variant");
        }
        Set<String> skus = new HashSet<>();
        for (SaveAdminProductVariantRequest variant : variants) {
            String sku = normalizeSku(variant.sku());
            if (!skus.add(sku)) {
                throw new IllegalArgumentException("SKU variant bị trùng: " + sku);
            }
            validThreshold(variant.lowStockThreshold());
        }
    }

    private String normalizeSku(String sku) {
        String normalized = value(sku).trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("SKU variant là bắt buộc");
        }
        return normalized;
    }

    private int validThreshold(int threshold) {
        int value = threshold == 0 ? 5 : threshold;
        if (value < 1 || value > 9999) {
            throw new IllegalArgumentException("Ngưỡng cảnh báo phải từ 1 đến 9999");
        }
        return value;
    }

    private AdminCategoryResponse toCategoryResponse(CategoryDto category) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getProductCount(),
                category.getVisible(),
                category.getVisibleLabel(),
                category.getCreatedAt(),
                category.getIconId(),
                category.getDescription(),
                category.getImageUrl());
    }

    private DashboardLowStockResponse toDashboardLowStock(LowStockProductDto product) {
        return new DashboardLowStockResponse(
                product.getName(),
                product.getCategory(),
                product.getStock(),
                product.getLevel());
    }

    private AdminActionResultResponse toActionResult(com.furnisight.admin.catalog.AdminActionResponse response) {
        return new AdminActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private String productStatusInput(SaveAdminProductRequest request) {
        if (request.status() != null && !request.status().isBlank()) {
            return request.status();
        }
        return value(request.statusLabel());
    }

    private String slugFrom(String sku, String name) {
        if (sku != null && !sku.isBlank()) {
            return sku;
        }
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.trim().toLowerCase()
                .transform(AdminCatalogService::stripAccents)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    private static String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }
}
