package com.furnisight.admin.service;

import com.furnisight.admin.catalog.CategoryDto;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.CreateCategoryRequest;
import com.furnisight.admin.catalog.CreateProductRequest;
import com.furnisight.admin.catalog.LowStockProductDto;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.ProductPageResponse;
import com.furnisight.admin.catalog.UpdateCategoryRequest;
import com.furnisight.admin.catalog.UpdateProductRequest;
import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminCategoryListResponse;
import com.furnisight.admin.controller.dto.AdminCategoryResponse;
import com.furnisight.admin.controller.dto.AdminProductPageResponse;
import com.furnisight.admin.controller.dto.AdminProductResponse;
import com.furnisight.admin.controller.dto.DashboardLowStockResponse;
import com.furnisight.admin.controller.dto.SaveAdminCategoryRequest;
import com.furnisight.admin.controller.dto.SaveAdminProductRequest;
import com.furnisight.admin.integration.GrpcAdminCatalogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

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
        return toActionResult(grpcAdminCatalogClient.createProduct(CreateProductRequest.newBuilder()
                .setName(value(request.name()))
                .setSlug(slugFrom(request.sku(), request.name()))
                .setCategory(value(request.category()))
                .setPrice(request.price())
                .setStock(request.stock())
                .setSku(value(request.sku()))
                .setStatus(productStatusInput(request))
                .setDescription(value(request.description()))
                .setModel3DUrl(value(request.model3dUrl()))
                .setModel3DFileName(value(request.model3dFileName()))
                .setModel3DSize(request.model3dSize())
                .addAllImageUrls(cleanList(request.imageUrls()))
                .build()));
    }

    public AdminActionResultResponse updateProduct(String id, SaveAdminProductRequest request) {
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
                .setModel3DUrl(value(request.model3dUrl()))
                .setModel3DFileName(value(request.model3dFileName()))
                .setModel3DSize(request.model3dSize())
                .addAllImageUrls(cleanList(request.imageUrls()))
                .build()));
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
                product.getModel3DUrl(),
                product.getModel3DFileName(),
                product.getModel3DSize(),
                product.getImageUrlsList());
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
                category.getDescription());
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
