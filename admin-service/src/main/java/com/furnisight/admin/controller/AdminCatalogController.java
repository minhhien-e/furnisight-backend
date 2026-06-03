package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminCategoryListResponse;
import com.furnisight.admin.controller.dto.AdminInventoryResponse;
import com.furnisight.admin.controller.dto.AdminProductPageResponse;
import com.furnisight.admin.controller.dto.AdminProductResponse;
import com.furnisight.admin.controller.dto.InventoryWarningSettingsResponse;
import com.furnisight.admin.controller.dto.SaveAdminCategoryRequest;
import com.furnisight.admin.controller.dto.SaveInventoryWarningSettingsRequest;
import com.furnisight.admin.controller.dto.SaveAdminProductRequest;
import com.furnisight.admin.controller.dto.StockInVariantRequest;
import com.furnisight.admin.security.CurrentUserProvider;
import com.furnisight.admin.service.AdminAuditLogService;
import com.furnisight.admin.service.AdminCatalogService;
import com.furnisight.admin.service.AdminInventorySettingsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;
    private final AdminInventorySettingsService adminInventorySettingsService;
    private final AdminAuditLogService adminAuditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminProductPageResponse> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(adminCatalogService.getProducts(page, size, query, status, category));
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminProductResponse> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(adminCatalogService.getProduct(id));
    }

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE') or hasAuthority('product_create') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> createProduct(@RequestBody SaveAdminProductRequest request,
            HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminCatalogService.createProduct(request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "create", "Tạo sản phẩm", "PRODUCT",
                request.sku(), result, "Tên sản phẩm: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT') or hasAuthority('product_edit') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateProduct(@PathVariable String id,
            @RequestBody SaveAdminProductRequest request,
            HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminCatalogService.updateProduct(id, request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật sản phẩm", "PRODUCT",
                id, result, "Tên sản phẩm: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE') or hasAuthority('product_delete') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteProduct(@PathVariable String id, HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminCatalogService.deleteProduct(id);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "delete", "Xóa sản phẩm", "PRODUCT",
                id, result, "Product id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminInventoryResponse> getInventory(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(adminCatalogService.getInventory(query));
    }

    @PostMapping("/inventory/stock-in")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT') or hasAuthority('product_edit') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> stockInVariant(@RequestBody StockInVariantRequest request,
            HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminCatalogService.stockInVariant(request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Nhập kho", "INVENTORY",
                request.variantId(), result, "Số lượng: " + request.quantity() + ", sản phẩm: " + request.productId(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/inventory/warning-settings")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<InventoryWarningSettingsResponse> getInventoryWarningSettings() {
        return ResponseEntity.ok(adminInventorySettingsService.getSettings());
    }

    @PutMapping("/inventory/warning-settings")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT') or hasAuthority('product_edit') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<InventoryWarningSettingsResponse> updateInventoryWarningSettings(
            @RequestBody SaveInventoryWarningSettingsRequest request,
            HttpServletRequest httpRequest) {
        InventoryWarningSettingsResponse response = adminInventorySettingsService.saveSettings(request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật cảnh báo tồn kho",
                "INVENTORY_SETTINGS", null, true, "Ngưỡng mặc định: " + response.defaultThreshold(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminCategoryListResponse> getCategories(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(adminCatalogService.getCategories(query));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE') or hasAuthority('product_create') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> createCategory(@RequestBody SaveAdminCategoryRequest request,
            HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminCatalogService.createCategory(request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "create", "Tạo danh mục", "CATEGORY",
                request.slug(), result, "Tên danh mục: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT') or hasAuthority('product_edit') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateCategory(@PathVariable String id,
            @RequestBody SaveAdminCategoryRequest request,
            HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminCatalogService.updateCategory(id, request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật danh mục", "CATEGORY",
                id, result, "Tên danh mục: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE') or hasAuthority('product_delete') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteCategory(@PathVariable String id, HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminCatalogService.deleteCategory(id);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "delete", "Xóa danh mục", "CATEGORY",
                id, result, "Category id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categories/icon-options")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminCategoryListResponse> getCategoryIconOptions() {
        return ResponseEntity.ok(new AdminCategoryListResponse(java.util.List.of()));
    }
}
