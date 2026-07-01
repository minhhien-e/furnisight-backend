package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.adapter.in.web.dto.ActionResponse;
import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.usecase.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.domain.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/marketing")
public class InternalMarketingController {
    private final GetCampaignsUseCase getCampaignsUseCase;
    private final CreateCampaignUseCase createCampaignUseCase;
    private final UpdateCampaignUseCase updateCampaignUseCase;
    private final DeleteCampaignUseCase deleteCampaignUseCase;
    private final GetCombosUseCase getCombosUseCase;
    private final CreateComboUseCase createComboUseCase;
    private final UpdateComboUseCase updateComboUseCase;
    private final DeleteComboUseCase deleteComboUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final UpdateNotificationUseCase updateNotificationUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;

    @GetMapping("/campaigns")
    public ResponseEntity<PageResponse<MarketingCampaignDto>> getCampaigns(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(getCampaignsUseCase.getCampaigns(GetCampaignsQuery.builder().query(query).status(status).build()));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ActionResponse> createCampaign(@RequestBody SaveMarketingCampaignCommand command) {
        createCampaignUseCase.createCampaign(command);
        return ResponseEntity.ok(new ActionResponse(true, "Campaign saved"));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<ActionResponse> updateCampaign(@PathVariable UUID id, @RequestBody SaveMarketingCampaignCommand command) {
        updateCampaignUseCase.updateCampaign(UpdateCampaignQuery.builder().id(id).command(command).build());
        return ResponseEntity.ok(new ActionResponse(true, "Campaign updated"));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<ActionResponse> deleteCampaign(@PathVariable UUID id) {
        deleteCampaignUseCase.deleteCampaign(DeleteCampaignQuery.builder().id(id).build());
        return ResponseEntity.ok(new ActionResponse(true, "Campaign deleted"));
    }

    @GetMapping("/combos")
    public ResponseEntity<PageResponse<MarketingComboDto>> getCombos(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(getCombosUseCase.getCombos(GetCombosQuery.builder().query(query).status(status).build()));
    }

    @PostMapping("/combos")
    public ResponseEntity<ActionResponse> createCombo(@RequestBody SaveMarketingComboCommand command) {
        createComboUseCase.createCombo(command);
        return ResponseEntity.ok(new ActionResponse(true, "Combo saved"));
    }

    @PutMapping("/combos/{id}")
    public ResponseEntity<ActionResponse> updateCombo(@PathVariable UUID id, @RequestBody SaveMarketingComboCommand command) {
        updateComboUseCase.updateCombo(UpdateComboQuery.builder().id(id).command(command).build());
        return ResponseEntity.ok(new ActionResponse(true, "Combo updated"));
    }

    @DeleteMapping("/combos/{id}")
    public ResponseEntity<ActionResponse> deleteCombo(@PathVariable UUID id) {
        deleteComboUseCase.deleteCombo(DeleteComboQuery.builder().id(id).build());
        return ResponseEntity.ok(new ActionResponse(true, "Combo deleted"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<PageResponse<MarketingNotificationDto>> getNotifications(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(getNotificationsUseCase.getNotifications(GetNotificationsQuery.builder().query(query).status(status).build()));
    }

    @PostMapping("/notifications")
    public ResponseEntity<ActionResponse> createNotification(@RequestBody SaveMarketingNotificationCommand command) {
        createNotificationUseCase.createNotification(command);
        return ResponseEntity.ok(new ActionResponse(true, "Notification saved"));
    }

    @PutMapping("/notifications/{id}")
    public ResponseEntity<ActionResponse> updateNotification(@PathVariable UUID id, @RequestBody SaveMarketingNotificationCommand command) {
        updateNotificationUseCase.updateNotification(UpdateNotificationQuery.builder().id(id).command(command).build());
        return ResponseEntity.ok(new ActionResponse(true, "Notification updated"));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<ActionResponse> deleteNotification(@PathVariable UUID id) {
        deleteNotificationUseCase.deleteNotification(DeleteNotificationQuery.builder().id(id).build());
        return ResponseEntity.ok(new ActionResponse(true, "Notification deleted"));
    }
}
