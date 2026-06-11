package com.furnisight.admin.voucher.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.voucher.application.VoucherService;
import com.furnisight.admin.voucher.web.dto.request.UpsertVoucherRequest;
import com.furnisight.admin.voucher.web.dto.response.VoucherListResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherStatsResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('order_view') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<VoucherListResponse> getVouchers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(voucherService.getVouchers(query, type, status));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('order_view') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<VoucherStatsResponse> getStats() {
        return ResponseEntity.ok(voucherService.getStats());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> createVoucher(
            @RequestBody UpsertVoucherRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.createVoucher(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "create", "Tạo voucher", "VOUCHER",
                request.code(), result, "Tên voucher: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> updateVoucher(
            @PathVariable String id, @RequestBody UpsertVoucherRequest request,
            HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.updateVoucher(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "update", "Cập nhật voucher", "VOUCHER",
                id, result, "Mã voucher: " + request.code(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> deleteVoucher(
            @PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.deleteVoucher(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "delete", "Xóa voucher", "VOUCHER",
                id, result, "Voucher id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('order_update') or hasAuthority('MANAGE_ORDERS') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> publishVoucher(
            @PathVariable String id, @RequestBody Object request, HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.publishVoucher(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), "send", "Phát hành voucher", "VOUCHER_PUBLISH",
                id, result, "Voucher id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }
}
