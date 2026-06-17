package com.furnisight.admin.catalog.inventory.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.catalog.inventory.application.InventoryService;
import com.furnisight.admin.catalog.inventory.web.dto.request.StockInVariantRequest;
import com.furnisight.admin.catalog.inventory.web.dto.request.UpdateVariantThresholdRequest;
import com.furnisight.admin.catalog.inventory.web.dto.response.InventoryResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<InventoryResponse> getInventory(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(inventoryService.getInventory(query));
    }

    @PostMapping("/stock-in")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> stockInVariant(
            @RequestBody StockInVariantRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = inventoryService.stockInVariant(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Nhập kho", "INVENTORY",
                request.variantId(), result,
                "Số lượng: " + request.quantity() + ", sản phẩm: " + request.productId(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/variants/{variantId}/threshold")
    @PreAuthorize("hasAuthority(\'PRODUCT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> updateVariantThreshold(
            @PathVariable String variantId, @RequestBody UpdateVariantThresholdRequest request,
            HttpServletRequest httpRequest) {
        ActionResultResponse response = inventoryService.updateVariantThreshold(
                variantId, request.lowStockThreshold());
        auditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật cảnh báo tồn kho",
                "PRODUCT_VARIANT", variantId, response,
                "Ngưỡng cảnh báo: " + request.lowStockThreshold(), httpRequest);
        return ResponseEntity.ok(response);
    }
}
