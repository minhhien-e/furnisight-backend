package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminOrderPageResponse;
import com.furnisight.admin.controller.dto.UpdateAdminOrderRequest;
import com.furnisight.admin.security.CurrentUserProvider;
import com.furnisight.admin.service.AdminAuditLogService;
import com.furnisight.admin.service.AdminOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final CurrentUserProvider currentUserProvider;
    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('order_view') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminOrderPageResponse> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(adminOrderService.getOrders(page, size, status, query));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateOrder(@PathVariable String id,
            @RequestBody UpdateAdminOrderRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        String status = request.status() != null && !request.status().isBlank() ? request.status() : request.statusLabel();
        AdminActionResultResponse result = adminOrderService.updateOrderStatus(adminId, id, status);
        adminAuditLogService.record(adminId, "update", "Cập nhật trạng thái đơn hàng", "ORDER", id, result,
                "Trạng thái: " + status, httpRequest);
        return ResponseEntity.ok(result);
    }
}
