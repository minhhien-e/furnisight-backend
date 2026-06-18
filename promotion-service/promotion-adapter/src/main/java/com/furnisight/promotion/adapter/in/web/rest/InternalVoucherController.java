package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.adapter.in.web.dto.ActionResponse;
import com.furnisight.promotion.adapter.in.web.dto.SavePromotionRequest;
import com.furnisight.promotion.application.dto.SavePromotionCommand;
import com.furnisight.promotion.application.dto.PromotionDto;
import com.furnisight.promotion.application.dto.ValidateOrderVouchersCommand;
import com.furnisight.promotion.application.dto.ValidateOrderVouchersResponse;
import com.furnisight.promotion.application.dto.ValidateComboCommand;
import com.furnisight.promotion.application.dto.ValidateComboResponse;
import com.furnisight.promotion.application.dto.VoucherStatsResponse;
import com.furnisight.promotion.application.dto.PublishVoucherCommand;
import com.furnisight.promotion.application.service.MarketingService;
import com.furnisight.promotion.application.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InternalVoucherController {
    private final PromotionService promotionService;
    private final MarketingService marketingService;

    @GetMapping("/internal/admin/vouchers")
    public ResponseEntity<List<PromotionDto>> getAdminVouchers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(promotionService.getAdminVouchers(query, type, status));
    }

    @GetMapping("/internal/admin/vouchers/stats")
    public ResponseEntity<VoucherStatsResponse> getStats() {
        return ResponseEntity.ok(promotionService.getStats());
    }

    @PostMapping("/internal/admin/vouchers")
    public ResponseEntity<ActionResponse> createVoucher(@RequestBody SavePromotionRequest request) {
        promotionService.createVoucher(toCommand(null, request));
        return ResponseEntity.ok(new ActionResponse(true, "Voucher created"));
    }

    @PutMapping("/internal/admin/vouchers/{id}")
    public ResponseEntity<ActionResponse> updateVoucher(@PathVariable UUID id, @RequestBody SavePromotionRequest request) {
        promotionService.updateVoucher(id, toCommand(id.toString(), request));
        return ResponseEntity.ok(new ActionResponse(true, "Voucher updated"));
    }

    @DeleteMapping("/internal/admin/vouchers/{id}")
    public ResponseEntity<ActionResponse> deleteVoucher(@PathVariable UUID id) {
        promotionService.deleteVoucher(id);
        return ResponseEntity.ok(new ActionResponse(true, "Voucher deleted"));
    }

    @PostMapping("/internal/admin/vouchers/{id}/publish")
    public ResponseEntity<ActionResponse> publishVoucher(@PathVariable UUID id, @RequestBody PublishVoucherCommand command) {
        var result = marketingService.publishVoucher(id, command);
        return ResponseEntity.ok(new ActionResponse(true, "Voucher publish accepted: " + result.acceptedCount()));
    }

    @PostMapping("/internal/vouchers/validate-order")
    public ResponseEntity<ValidateOrderVouchersResponse> validateOrderVouchers(@RequestBody ValidateOrderVouchersCommand command) {
        return ResponseEntity.ok(promotionService.validateOrderVouchers(command));
    }

    @PostMapping("/internal/combos/validate-order")
    public ResponseEntity<ValidateComboResponse> validateOrderCombo(@RequestBody ValidateComboCommand command) {
        return ResponseEntity.ok(marketingService.validateCombo(command));
    }

    private SavePromotionCommand toCommand(String id, SavePromotionRequest request) {
        return SavePromotionCommand.builder()
                .id(id)
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .icon(request.icon())
                .voucherType(request.voucherType())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .maxDiscount(request.maxDiscount())
                .minOrder(request.minOrder())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .active(request.active())
                .placements(request.placements())
                .build();
    }
}
