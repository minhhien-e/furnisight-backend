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
            @RequestBody CreateNotificationTemplateRequest request,
            HttpServletRequest httpRequest) {
        NotificationTemplateResponse response = notificationTemplateService.createTemplate(request);
        audit("create", "Tạo mẫu thông báo", "NOTIFICATION_TEMPLATE", new ActionResultResponse(true, "Mẫu thông báo đã được tạo"), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable UUID templateId,
            @RequestBody UpdateNotificationTemplateRequest request,
            HttpServletRequest httpRequest) {
        NotificationTemplateResponse response = notificationTemplateService.updateTemplate(templateId, request);
        audit("update", "Cập nhật mẫu thông báo", "NOTIFICATION_TEMPLATE", new ActionResultResponse(true, "Mẫu thông báo đã được cập nhật"), httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteTemplate(
            @PathVariable UUID templateId,
            HttpServletRequest httpRequest) {
        ActionResultResponse response = notificationTemplateService.deleteTemplate(templateId);
        audit("delete", "Xóa mẫu thông báo", "NOTIFICATION_TEMPLATE", response, httpRequest);
        return ResponseEntity.ok(response);
    }

    private void audit(String actionType, String action, String resourceType, ActionResultResponse result, HttpServletRequest request) {
        auditLogService.record(currentUserProvider.getCurrentUserId(), actionType, action, resourceType, null, result, "", request);
    }
}
