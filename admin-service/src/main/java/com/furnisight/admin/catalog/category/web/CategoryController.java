package com.furnisight.admin.catalog.category.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.catalog.category.application.CategoryService;
import com.furnisight.admin.catalog.category.web.dto.request.UpsertCategoryRequest;
import com.furnisight.admin.catalog.category.web.dto.response.CategoryListResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<CategoryListResponse> getCategories(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(categoryService.getCategories(query));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> createCategory(
            @RequestBody UpsertCategoryRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = categoryService.createCategory(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "create", "Tạo danh mục", "CATEGORY",
                request.slug(), result, "Tên danh mục: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> updateCategory(
            @PathVariable String id, @RequestBody UpsertCategoryRequest request,
            HttpServletRequest httpRequest) {
        ActionResultResponse result = categoryService.updateCategory(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật danh mục", "CATEGORY",
                id, result, "Tên danh mục: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> deleteCategory(
            @PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = categoryService.deleteCategory(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "delete", "Xóa danh mục", "CATEGORY",
                id, result, "Category id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/icon-options")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<CategoryListResponse> getCategoryIconOptions() {
        return ResponseEntity.ok(new CategoryListResponse(java.util.List.of()));
    }
}
