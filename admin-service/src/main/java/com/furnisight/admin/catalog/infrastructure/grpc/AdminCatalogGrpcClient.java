package com.furnisight.admin.catalog.infrastructure.grpc;

import com.furnisight.admin.catalog.AdminActionResponse;
import com.furnisight.admin.catalog.AdminCatalogServiceGrpc;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.CategoryStatsResponse;
import com.furnisight.admin.catalog.CreateCategoryRequest;
import com.furnisight.admin.catalog.CreateProductRequest;
import com.furnisight.admin.catalog.DeleteCategoryRequest;
import com.furnisight.admin.catalog.DeleteProductRequest;
import com.furnisight.admin.catalog.GetAdminCategoriesRequest;
import com.furnisight.admin.catalog.GetAdminProductsRequest;
import com.furnisight.admin.catalog.GetLowStockProductsRequest;
import com.furnisight.admin.catalog.GetProductDetailRequest;
import com.furnisight.admin.catalog.LowStockProductListResponse;
import com.furnisight.admin.catalog.ProductPageResponse;
import com.furnisight.admin.catalog.ProductStatsResponse;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.StockInVariantRequest;
import com.furnisight.admin.catalog.UpdateCategoryRequest;
import com.furnisight.admin.catalog.UpdateProductRequest;
import com.furnisight.admin.catalog.UpdateVariantThresholdRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class AdminCatalogGrpcClient {

    @GrpcClient("catalog-service")
    private AdminCatalogServiceGrpc.AdminCatalogServiceBlockingStub adminCatalogServiceStub;

    public ProductPageResponse getProducts(int page, int size, String query, String status, String category) {
        return adminCatalogServiceStub.getAdminProducts(GetAdminProductsRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setQuery(query == null ? "" : query)
                .setStatus(status == null ? "" : status)
                .setCategory(category == null ? "" : category)
                .build());
    }

    public ProductStatsResponse getProductStats() {
        return adminCatalogServiceStub.getProductStats(com.google.protobuf.Empty.getDefaultInstance());
    }

    public ProductDto getProductDetail(String id) {
        return adminCatalogServiceStub.getProductDetail(GetProductDetailRequest.newBuilder()
                .setId(id == null ? "" : id)
                .build());
    }

    public LowStockProductListResponse getLowStockProducts(int limit, int threshold) {
        return adminCatalogServiceStub.getLowStockProducts(GetLowStockProductsRequest.newBuilder()
                .setLimit(limit)
                .setThreshold(threshold)
                .build());
    }

    public AdminActionResponse createProduct(CreateProductRequest request) {
        return adminCatalogServiceStub.createProduct(request);
    }

    public AdminActionResponse updateProduct(UpdateProductRequest request) {
        return adminCatalogServiceStub.updateProduct(request);
    }

    public AdminActionResponse deleteProduct(String id) {
        return adminCatalogServiceStub.deleteProduct(DeleteProductRequest.newBuilder().setId(id == null ? "" : id).build());
    }

    public AdminActionResponse stockInVariant(StockInVariantRequest request) {
        return adminCatalogServiceStub.stockInVariant(request);
    }

    public AdminActionResponse updateVariantThreshold(String variantId, int threshold) {
        return adminCatalogServiceStub.updateVariantThreshold(UpdateVariantThresholdRequest.newBuilder()
                .setVariantId(variantId)
                .setLowStockThreshold(threshold)
                .build());
    }

    public CategoryListResponse getCategories(String query) {
        return adminCatalogServiceStub.getAdminCategories(GetAdminCategoriesRequest.newBuilder()
                .setQuery(query == null ? "" : query)
                .build());
    }

    public CategoryStatsResponse getCategoryStats() {
        return adminCatalogServiceStub.getCategoryStats(com.google.protobuf.Empty.getDefaultInstance());
    }

    public AdminActionResponse createCategory(CreateCategoryRequest request) {
        return adminCatalogServiceStub.createCategory(request);
    }

    public AdminActionResponse updateCategory(UpdateCategoryRequest request) {
        return adminCatalogServiceStub.updateCategory(request);
    }

    public AdminActionResponse deleteCategory(String id) {
        return adminCatalogServiceStub.deleteCategory(DeleteCategoryRequest.newBuilder().setId(id == null ? "" : id).build());
    }
}
