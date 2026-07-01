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
import com.furnisight.promotion.application.port.in.usecase.*;
import com.furnisight.promotion.application.port.in.query.*;
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
    private final ValidateOrderVouchersUseCase validateOrderVouchersUseCase;
    private final ValidateComboUseCase validateComboUseCase;
    private final PublishVoucherUseCase publishVoucherUseCase;
    private final GetAdminVouchersUseCase getAdminVouchersUseCase;
    private final GetPromotionStatsUseCase getPromotionStatsUseCase;
    private final CreateVoucherUseCase createVoucherUseCase;
    private final UpdateVoucherUseCase updateVoucherUseCase;
    private final DeleteVoucherUseCase deleteVoucherUseCase;

    @GetMapping("/internal/admin/vouchers")
    public ResponseEntity<List<PromotionDto>> getAdminVouchers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(getAdminVouchersUseCase.getAdminVouchers(GetAdminVouchersQuery.builder().query(query).type(type).status(status).build()));
    }

    @GetMapping("/internal/admin/vouchers/stats")
    public ResponseEntity<VoucherStatsResponse> getStats() {
        return ResponseEntity.ok(getPromotionStatsUseCase.getStats(new GetPromotionStatsQuery()));
    }

    @PostMapping("/internal/admin/vouchers")
    public ResponseEntity<ActionResponse> createVoucher(@RequestBody SavePromotionRequest request) {
        createVoucherUseCase.createVoucher(toCommand(null, request));
        return ResponseEntity.ok(new ActionResponse(true, "Voucher created"));
    }

    @PutMapping("/internal/admin/vouchers/{id}")
    public ResponseEntity<ActionResponse> updateVoucher(@PathVariable UUID id, @RequestBody SavePromotionRequest request) {
        updateVoucherUseCase.updateVoucher(UpdateVoucherQuery.builder().id(id).command(toCommand(id.toString(), request)).build());
        return ResponseEntity.ok(new ActionResponse(true, "Voucher updated"));
    }

    @DeleteMapping("/internal/admin/vouchers/{id}")
    public ResponseEntity<ActionResponse> deleteVoucher(@PathVariable UUID id) {
        deleteVoucherUseCase.deleteVoucher(DeleteVoucherQuery.builder().id(id).build());
        return ResponseEntity.ok(new ActionResponse(true, "Voucher deleted"));
    }

    @PostMapping("/internal/admin/vouchers/{id}/publish")
    public ResponseEntity<ActionResponse> publishVoucher(@PathVariable UUID id, @RequestBody PublishVoucherCommand command) {
        var result = publishVoucherUseCase.publishVoucher(PublishVoucherQuery.builder().voucherId(id).command(command).build());
        return ResponseEntity.ok(new ActionResponse(true, "Voucher publish accepted: " + result.acceptedCount()));
    }

    @PostMapping("/internal/vouchers/validate-order")
    public ResponseEntity<ValidateOrderVouchersResponse> validateOrderVouchers(@RequestBody ValidateOrderVouchersCommand command) {
        return ResponseEntity.ok(validateOrderVouchersUseCase.validateOrderVouchers(command));
    }

    @PostMapping("/internal/combos/validate-order")
    public ResponseEntity<ValidateComboResponse> validateOrderCombo(@RequestBody ValidateComboCommand command) {
        return ResponseEntity.ok(validateComboUseCase.validateCombo(command));
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
                .build();
    }
}
