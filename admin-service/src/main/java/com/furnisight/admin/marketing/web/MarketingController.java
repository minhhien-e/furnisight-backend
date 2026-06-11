package com.furnisight.admin.marketing.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.marketing.application.MarketingService;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
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
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Object> getCampaigns(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getCampaigns(query, status));
    }

    @PostMapping("/campaigns")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> createCampaign(@RequestBody Object request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.createCampaign(request);
        audit("create", "Tạo chiến dịch marketing", "MARKETING_CAMPAIGN", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/campaigns/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> updateCampaign(@PathVariable String id, @RequestBody Object request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.updateCampaign(id, request);
        audit("update", "Cập nhật chiến dịch marketing", "MARKETING_CAMPAIGN", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/campaigns/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> deleteCampaign(@PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.deleteCampaign(id);
        audit("delete", "Xóa chiến dịch marketing", "MARKETING_CAMPAIGN", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/combos")
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Object> getCombos(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getCombos(query, status));
    }

    @PostMapping("/combos")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> createCombo(@RequestBody Object request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.createCombo(request);
        audit("create", "Tạo combo khuyến mãi", "PROMOTION_COMBO", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/combos/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> updateCombo(@PathVariable String id, @RequestBody Object request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.updateCombo(id, request);
        audit("update", "Cập nhật combo khuyến mãi", "PROMOTION_COMBO", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/combos/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> deleteCombo(@PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.deleteCombo(id);
        audit("delete", "Xóa combo khuyến mãi", "PROMOTION_COMBO", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAuthority('ORDER_VIEW') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Object> getNotifications(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getNotifications(query, status));
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> createNotification(@RequestBody Object request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.createNotification(request);
        audit("create", "Tạo thông báo marketing", "MARKETING_NOTIFICATION", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/notifications/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> updateNotification(@PathVariable String id, @RequestBody Object request, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.updateNotification(id, request);
        audit("update", "Cập nhật thông báo marketing", "MARKETING_NOTIFICATION", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/notifications/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> deleteNotification(@PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = marketingService.deleteNotification(id);
        audit("delete", "Xóa thông báo marketing", "MARKETING_NOTIFICATION", result, httpRequest);
        return ResponseEntity.ok(result);
    }

    private void audit(String actionType, String action, String resourceType, ActionResultResponse result, HttpServletRequest request) {
        auditLogService.record(currentUserProvider.getCurrentUserId(), actionType, action, resourceType, null, result, "", request);
    }
}
