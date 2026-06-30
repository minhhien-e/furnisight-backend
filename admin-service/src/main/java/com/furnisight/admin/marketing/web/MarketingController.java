package com.furnisight.admin.marketing.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.marketing.application.MarketingService;
import com.furnisight.admin.marketing.web.dto.MarketingCampaignRequest;
import com.furnisight.admin.marketing.web.dto.MarketingCampaignResponse;
import com.furnisight.admin.marketing.web.dto.MarketingComboRequest;
import com.furnisight.admin.marketing.web.dto.MarketingComboResponse;
import com.furnisight.admin.marketing.web.dto.MarketingNotificationRequest;
import com.furnisight.admin.marketing.web.dto.MarketingNotificationResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/marketing")
@RequiredArgsConstructor
public class MarketingController {
    private final MarketingService marketingService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/campaigns")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<MarketingCampaignResponse>> getCampaigns(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getCampaigns(query, status));
    }

    @PostMapping("/campaigns")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> createCampaign(@RequestBody MarketingCampaignRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.createCampaign(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.CREATE_CAMPAIGN, null, result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/campaigns/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> updateCampaign(@PathVariable String id, @RequestBody MarketingCampaignRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.updateCampaign(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.UPDATE_CAMPAIGN, id, result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/campaigns/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteCampaign(@PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.deleteCampaign(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.DELETE_CAMPAIGN, id, result, null, httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/combos")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<MarketingComboResponse>> getCombos(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getCombos(query, status));
    }

    @PostMapping("/combos")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> createCombo(@RequestBody MarketingComboRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.createCombo(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.CREATE_COMBO, null, result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/combos/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> updateCombo(@PathVariable String id, @RequestBody MarketingComboRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.updateCombo(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.UPDATE_COMBO, id, result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/combos/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteCombo(@PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.deleteCombo(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.DELETE_COMBO, id, result, null, httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<MarketingNotificationResponse>> getNotifications(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getNotifications(query, status));
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> createNotification(@RequestBody MarketingNotificationRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.createNotification(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.CREATE_MARKETING_NOTIFICATION, null, result, request.title(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/notifications/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> updateNotification(@PathVariable String id, @RequestBody MarketingNotificationRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.updateNotification(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.UPDATE_MARKETING_NOTIFICATION, id, result, request.title(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/notifications/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_SUPPORT') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteNotification(@PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.deleteNotification(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.DELETE_MARKETING_NOTIFICATION, id, result, null, httpRequest);
        return ResponseEntity.ok(result);
    }
}
