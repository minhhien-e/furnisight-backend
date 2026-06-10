package com.furnisight.admin.audit.web;

import com.furnisight.admin.audit.web.dto.response.AuditLogPageResponse;
import com.furnisight.admin.audit.application.AuditLogService;
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
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('MANAGE_ROLES') or hasAuthority('reports') or hasAuthority('REPORTS')")
    public ResponseEntity<AuditLogPageResponse> getAuditLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(auditLogService.getLogs(search, type, result, period, page, pageSize));
    }
}
