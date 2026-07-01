package com.furnisight.admin.voucher.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.voucher.application.VoucherService;
import com.furnisight.admin.voucher.web.dto.request.PublishVoucherRequest;
import com.furnisight.admin.voucher.web.dto.request.UpsertVoucherRequest;
import com.furnisight.admin.voucher.web.dto.response.VoucherResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherStatsResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('VOUCHER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<List<VoucherResponse>> getVouchers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(voucherService.getVouchers(query, type, status));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('VOUCHER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<VoucherStatsResponse> getStats() {
        return ResponseEntity.ok(voucherService.getStats());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VOUCHER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> createVoucher(
            @Valid @RequestBody UpsertVoucherRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.createVoucher(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.CREATE_VOUCHER,
                request.code(), result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> updateVoucher(
            @PathVariable String id, @Valid @RequestBody UpsertVoucherRequest request,
            HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.updateVoucher(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.UPDATE_VOUCHER,
                id, result, request.code(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteVoucher(
            @PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.deleteVoucher(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.DELETE_VOUCHER,
                id, result, null, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('VOUCHER_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> publishVoucher(
            @PathVariable String id, @Valid @RequestBody PublishVoucherRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = voucherService.publishVoucher(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.PUBLISH_VOUCHER,
                id, result, null, httpRequest);
        return ResponseEntity.ok(result);
    }
}
