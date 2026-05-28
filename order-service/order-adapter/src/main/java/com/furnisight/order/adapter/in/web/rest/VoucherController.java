package com.furnisight.order.adapter.in.web.rest;

import com.furnisight.order.application.common.port.in.CurrentUserProvider;
import com.furnisight.order.application.promotion.port.in.dto.PromotionDto;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherCommand;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherResponse;
import com.furnisight.order.application.promotion.port.in.usecase.VoucherUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherUseCase voucherUseCase;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/user")
    public ResponseEntity<List<PromotionDto>> getAvailableVouchers() {
        UUID userId = currentUserProvider.getCurrentUserId();
        List<PromotionDto> vouchers = voucherUseCase.getAvailableVouchers(userId);
        return ResponseEntity.ok(vouchers);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateVoucherResponse> validateVoucher(@RequestBody ValidateVoucherCommand command) {
        UUID userId = currentUserProvider.getCurrentUserId();
        command.setUserId(userId);
        ValidateVoucherResponse response = voucherUseCase.validateVoucher(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{code}/save")
    public ResponseEntity<Void> saveVoucher(@PathVariable String code) {
        UUID userId = currentUserProvider.getCurrentUserId();
        voucherUseCase.saveVoucher(userId, code);
        return ResponseEntity.ok().build();
    }
}
