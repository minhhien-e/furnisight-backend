package com.furnisight.admin.catalog.infrastructure.grpc;

import com.furnisight.admin.catalog.AdminActionResponse;
import com.furnisight.admin.catalog.AdminCatalogServiceGrpc;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.CategoryStatsResponse;
import com.furnisight.admin.catalog.CreateCategoryRequest;
import com.furnisight.admin.catalog.CreateProductRequest;
import com.furnisight.admin.catalog.DeleteCategoryRequest;
import com.furnisight.admin.catalog.DeleteProductRequest;
import com.furnisight.admin.catalog.DeleteRoomTypeRequest;
import com.furnisight.admin.catalog.GetAdminCategoriesRequest;
import com.furnisight.admin.catalog.GetAdminProductsRequest;
import com.furnisight.admin.catalog.GetAdminRoomTypesRequest;
import com.furnisight.admin.catalog.GetRoomTypeDetailRequest;
import com.furnisight.admin.catalog.GetLowStockProductsRequest;
import com.furnisight.admin.catalog.GetProductDetailRequest;
import com.furnisight.admin.catalog.LowStockProductListResponse;
import com.furnisight.admin.catalog.ProductPageResponse;
import com.furnisight.admin.catalog.ProductStatsResponse;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.RoomTypeDto;
import com.furnisight.admin.catalog.RoomTypeListResponse;
import com.furnisight.admin.catalog.CreateRoomTypeRequest;
import com.furnisight.admin.catalog.UpdateRoomTypeRequest;
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

    public ProductStatsResponse getProductStats(String startDate, String endDate) {
        return adminCatalogServiceStub.getProductStats(com.furnisight.admin.catalog.GetProductStatsRequest.newBuilder()
                .setStartDate(startDate == null ? "" : startDate)
                .setEndDate(endDate == null ? "" : endDate)
                .build());
    }

    public ProductDto getProductDetail(String id) {
        return adminCatalogServiceStub.getProductDetail(GetProductDetailRequest.newBuilder()
                .setId(id == null ? "" : id)
                .build());
    }

    public LowStockProductListResponse getLowStockProducts(int limit) {
        return adminCatalogServiceStub.getLowStockProducts(GetLowStockProductsRequest.newBuilder()
                .setLimit(limit)
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

    public RoomTypeListResponse getRoomTypes(String query) {
        return adminCatalogServiceStub.getAdminRoomTypes(GetAdminRoomTypesRequest.newBuilder()
                .setQuery(query == null ? "" : query)
                .build());
    }

    public RoomTypeDto getRoomTypeDetail(String id) {
        return adminCatalogServiceStub.getRoomTypeDetail(GetRoomTypeDetailRequest.newBuilder()
                .setId(id == null ? "" : id)
                .build());
    }

    public AdminActionResponse createRoomType(CreateRoomTypeRequest request) {
        return adminCatalogServiceStub.createRoomType(request);
    }

    public AdminActionResponse updateRoomType(UpdateRoomTypeRequest request) {
        return adminCatalogServiceStub.updateRoomType(request);
    }

    public AdminActionResponse deleteRoomType(String id) {
        return adminCatalogServiceStub.deleteRoomType(DeleteRoomTypeRequest.newBuilder().setId(id == null ? "" : id).build());
    }
}
