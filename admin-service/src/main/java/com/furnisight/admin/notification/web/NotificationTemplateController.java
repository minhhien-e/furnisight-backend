package com.furnisight.admin.notification.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.notification.application.NotificationTemplateService;
import com.furnisight.admin.notification.web.dto.CreateNotificationTemplateRequest;
import com.furnisight.admin.notification.web.dto.NotificationTemplateResponse;
import com.furnisight.admin.notification.web.dto.UpdateNotificationTemplateRequest;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<List<NotificationTemplateResponse>> getTemplates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(notificationTemplateService.getTemplates(name, channel, type));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> getTemplateByCode(@PathVariable String code) {
        return ResponseEntity.ok(notificationTemplateService.getTemplateByCode(code));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody CreateNotificationTemplateRequest request,
            HttpServletRequest httpRequest) {
        NotificationTemplateResponse response = notificationTemplateService.createTemplate(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.CREATE_NOTIFICATION, null, new ActionResultResponse(true, "Mẫu thông báo đã được tạo"), request.getName(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateNotificationTemplateRequest request,
            HttpServletRequest httpRequest) {
        NotificationTemplateResponse response = notificationTemplateService.updateTemplate(templateId, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.UPDATE_NOTIFICATION, templateId.toString(), new ActionResultResponse(true, "Mẫu thông báo đã được cập nhật"), request.getName(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteTemplate(
            @PathVariable UUID templateId,
            HttpServletRequest httpRequest) {
        ActionResultResponse response = notificationTemplateService.deleteTemplate(templateId);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.DELETE_NOTIFICATION, templateId.toString(), response, null, httpRequest);
        return ResponseEntity.ok(response);
    }
}
