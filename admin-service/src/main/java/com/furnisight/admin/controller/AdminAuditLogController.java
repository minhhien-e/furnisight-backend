package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminAuditLogPageResponse;
import com.furnisight.admin.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('MANAGE_ROLES') or hasAuthority('reports') or hasAuthority('REPORTS')")
    public ResponseEntity<AdminAuditLogPageResponse> getAuditLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(adminAuditLogService.getLogs(search, type, result, period, page, pageSize));
    }
}
