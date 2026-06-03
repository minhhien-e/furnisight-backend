package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminCategoryListResponse;
import com.furnisight.admin.controller.dto.AdminInventoryResponse;
import com.furnisight.admin.controller.dto.AdminProductPageResponse;
import com.furnisight.admin.controller.dto.AdminProductResponse;
import com.furnisight.admin.controller.dto.SaveAdminCategoryRequest;
import com.furnisight.admin.controller.dto.SaveAdminProductRequest;
import com.furnisight.admin.controller.dto.StockInVariantRequest;
import com.furnisight.admin.service.AdminCatalogService;
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
    public ResponseEntity<AdminActionResultResponse> createProduct(@RequestBody SaveAdminProductRequest request) {
        return ResponseEntity.ok(adminCatalogService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT') or hasAuthority('product_edit') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateProduct(@PathVariable String id,
            @RequestBody SaveAdminProductRequest request) {
        return ResponseEntity.ok(adminCatalogService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE') or hasAuthority('product_delete') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteProduct(@PathVariable String id) {
        return ResponseEntity.ok(adminCatalogService.deleteProduct(id));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminInventoryResponse> getInventory(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(adminCatalogService.getInventory(query));
    }

    @PostMapping("/inventory/stock-in")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT') or hasAuthority('product_edit') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> stockInVariant(@RequestBody StockInVariantRequest request) {
        return ResponseEntity.ok(adminCatalogService.stockInVariant(request));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminCategoryListResponse> getCategories(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(adminCatalogService.getCategories(query));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE') or hasAuthority('product_create') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> createCategory(@RequestBody SaveAdminCategoryRequest request) {
        return ResponseEntity.ok(adminCatalogService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT') or hasAuthority('product_edit') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateCategory(@PathVariable String id,
            @RequestBody SaveAdminCategoryRequest request) {
        return ResponseEntity.ok(adminCatalogService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE') or hasAuthority('product_delete') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteCategory(@PathVariable String id) {
        return ResponseEntity.ok(adminCatalogService.deleteCategory(id));
    }

    @GetMapping("/categories/icon-options")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view') or hasAuthority('MANAGE_PRODUCTS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminCategoryListResponse> getCategoryIconOptions() {
        return ResponseEntity.ok(new AdminCategoryListResponse(java.util.List.of()));
    }
}
