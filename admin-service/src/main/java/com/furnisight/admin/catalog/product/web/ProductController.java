package com.furnisight.admin.catalog.product.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.catalog.product.application.ProductService;
import com.furnisight.admin.catalog.product.web.dto.request.UpsertProductRequest;
import com.furnisight.admin.catalog.product.web.dto.response.ProductPageResponse;
import com.furnisight.admin.catalog.product.web.dto.response.ProductResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ProductPageResponse> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(productService.getProducts(page, size, query, status, category));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> createProduct(
            @RequestBody UpsertProductRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = productService.createProduct(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "create", "Tạo sản phẩm", "PRODUCT",
                request.sku(), result, "Tên sản phẩm: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> updateProduct(
            @PathVariable String id, @RequestBody UpsertProductRequest request,
            HttpServletRequest httpRequest) {
        ActionResultResponse result = productService.updateProduct(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật sản phẩm", "PRODUCT",
                id, result, "Tên sản phẩm: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> deleteProduct(
            @PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = productService.deleteProduct(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "delete", "Xóa sản phẩm", "PRODUCT",
                id, result, "Product id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }
}
