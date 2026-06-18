package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.adapter.in.web.dto.ActionResponse;
import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.service.MarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/marketing")
public class InternalMarketingController {
    private final MarketingService marketingService;

    @GetMapping("/campaigns")
    public ResponseEntity<PageResponse<MarketingCampaignDto>> getCampaigns(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getCampaigns(query, status));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ActionResponse> createCampaign(@RequestBody SaveMarketingCampaignCommand command) {
        marketingService.createCampaign(command);
        return ResponseEntity.ok(new ActionResponse(true, "Campaign saved"));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<ActionResponse> updateCampaign(@PathVariable UUID id, @RequestBody SaveMarketingCampaignCommand command) {
        marketingService.updateCampaign(id, command);
        return ResponseEntity.ok(new ActionResponse(true, "Campaign updated"));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<ActionResponse> deleteCampaign(@PathVariable UUID id) {
        marketingService.deleteCampaign(id);
        return ResponseEntity.ok(new ActionResponse(true, "Campaign deleted"));
    }

    @GetMapping("/combos")
    public ResponseEntity<PageResponse<MarketingComboDto>> getCombos(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getCombos(query, status));
    }

    @PostMapping("/combos")
    public ResponseEntity<ActionResponse> createCombo(@RequestBody SaveMarketingComboCommand command) {
        marketingService.createCombo(command);
        return ResponseEntity.ok(new ActionResponse(true, "Combo saved"));
    }

    @PutMapping("/combos/{id}")
    public ResponseEntity<ActionResponse> updateCombo(@PathVariable UUID id, @RequestBody SaveMarketingComboCommand command) {
        marketingService.updateCombo(id, command);
        return ResponseEntity.ok(new ActionResponse(true, "Combo updated"));
    }

    @DeleteMapping("/combos/{id}")
    public ResponseEntity<ActionResponse> deleteCombo(@PathVariable UUID id) {
        marketingService.deleteCombo(id);
        return ResponseEntity.ok(new ActionResponse(true, "Combo deleted"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<PageResponse<MarketingNotificationDto>> getNotifications(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketingService.getNotifications(query, status));
    }

    @PostMapping("/notifications")
    public ResponseEntity<ActionResponse> createNotification(@RequestBody SaveMarketingNotificationCommand command) {
        marketingService.createNotification(command);
        return ResponseEntity.ok(new ActionResponse(true, "Notification saved"));
    }

    @PutMapping("/notifications/{id}")
    public ResponseEntity<ActionResponse> updateNotification(@PathVariable UUID id, @RequestBody SaveMarketingNotificationCommand command) {
        marketingService.updateNotification(id, command);
        return ResponseEntity.ok(new ActionResponse(true, "Notification updated"));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<ActionResponse> deleteNotification(@PathVariable UUID id) {
        marketingService.deleteNotification(id);
        return ResponseEntity.ok(new ActionResponse(true, "Notification deleted"));
    }
}
