package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminVoucherListResponse;
import com.furnisight.admin.controller.dto.SaveAdminVoucherRequest;
import com.furnisight.admin.security.CurrentUserProvider;
import com.furnisight.admin.service.AdminAuditLogService;
import com.furnisight.admin.service.AdminOrderService;
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
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminOrderService adminOrderService;
    private final AdminAuditLogService adminAuditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('order_view') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminVoucherListResponse> getVouchers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminOrderService.getVouchers(query, status));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> createVoucher(@RequestBody SaveAdminVoucherRequest request,
            HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminOrderService.createVoucher(request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "create", "Tạo voucher", "VOUCHER",
                request.code(), result, "Tên voucher: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateVoucher(@PathVariable String id,
            @RequestBody SaveAdminVoucherRequest request,
            HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminOrderService.updateVoucher(id, request);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật voucher", "VOUCHER",
                id, result, "Mã voucher: " + request.code(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteVoucher(@PathVariable String id, HttpServletRequest httpRequest) {
        AdminActionResultResponse result = adminOrderService.deleteVoucher(id);
        adminAuditLogService.record(currentUserProvider.getCurrentUserId(), "delete", "Xóa voucher", "VOUCHER",
                id, result, "Voucher id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }
}
