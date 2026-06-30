package com.furnisight.admin.order.web;

import com.furnisight.admin.order.application.OrderService;
import com.furnisight.admin.order.web.dto.request.UpdateOrderRequest;
import com.furnisight.admin.order.web.dto.response.OrderResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
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

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(orderService.getOrders(page, size, status, query));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> updateOrder(
            @PathVariable String id, @RequestBody UpdateOrderRequest request) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = orderService.updateOrderStatus(
                adminId, id, request.status(), request.trackingCode(), request.note()
        );
        return ResponseEntity.ok(result);
    }
}
