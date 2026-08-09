package com.furnisight.admin.message.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.message.application.MessageTemplateService;
import com.furnisight.admin.message.web.dto.request.UpsertMessageTemplateRequest;
import com.furnisight.admin.message.web.dto.response.MessageTemplateResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/message-templates")
@RequiredArgsConstructor
public class MessageTemplateController {

    private final MessageTemplateService messageTemplateService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<List<MessageTemplateResponse>> getTemplates() {
        return ResponseEntity.ok(messageTemplateService.getTemplates());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<MessageTemplateResponse> getTemplateById(@PathVariable Integer id) {
        return ResponseEntity.ok(messageTemplateService.getTemplateById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<MessageTemplateResponse> createTemplate(
            @Valid @RequestBody UpsertMessageTemplateRequest request,
            HttpServletRequest httpRequest) {
        MessageTemplateResponse response = messageTemplateService.createTemplate(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.CREATE_MESSAGE_TEMPLATE, null, new ActionResultResponse(true, "Mẫu tin nhắn đã được tạo"), request.getTitle(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<MessageTemplateResponse> updateTemplate(
            @PathVariable Integer id,
            @Valid @RequestBody UpsertMessageTemplateRequest request,
            HttpServletRequest httpRequest) {
        MessageTemplateResponse response = messageTemplateService.updateTemplate(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.UPDATE_MESSAGE_TEMPLATE, id.toString(), new ActionResultResponse(true, "Mẫu tin nhắn đã được cập nhật"), request.getTitle(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteTemplate(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        ActionResultResponse response = messageTemplateService.deleteTemplate(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.DELETE_MESSAGE_TEMPLATE, id.toString(), response, null, httpRequest);
        return ResponseEntity.ok(response);
    }
}
