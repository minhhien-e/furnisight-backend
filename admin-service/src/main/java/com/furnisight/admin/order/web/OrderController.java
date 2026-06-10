package com.furnisight.admin.order.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.order.application.OrderService;
import com.furnisight.admin.order.web.dto.request.UpdateOrderRequest;
import com.furnisight.admin.order.web.dto.response.OrderPageResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('order_view') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<OrderPageResponse> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(orderService.getOrders(page, size, status, query));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> updateOrder(
            @PathVariable String id, @RequestBody UpdateOrderRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = orderService.updateOrderStatus(adminId, id, request.status());
        auditLogService.record(adminId, "update", "Cập nhật trạng thái đơn hàng", "ORDER",
                id, result, "Trạng thái: " + request.status(), httpRequest);
        return ResponseEntity.ok(result);
    }
}
