package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.application.common.CurrentUserProvider;
import com.furnisight.promotion.application.dto.PromotionDto;
import com.furnisight.promotion.application.dto.ValidateVoucherCommand;
import com.furnisight.promotion.application.dto.ValidateVoucherResponse;
import com.furnisight.promotion.application.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final PromotionService promotionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/user")
    public ResponseEntity<List<PromotionDto>> getAvailableVouchers() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(promotionService.getAvailableVouchers(userId));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateVoucherResponse> validateVoucher(@RequestBody ValidateVoucherCommand command) {
        command.setUserId(currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(promotionService.validateVoucher(command));
    }

    @PostMapping("/{code}/save")
    public ResponseEntity<Void> saveVoucher(@PathVariable String code) {
        promotionService.saveVoucher(currentUserProvider.getCurrentUserId(), code);
        return ResponseEntity.ok().build();
    }
}
