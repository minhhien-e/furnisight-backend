package com.furnisight.order.application.promotion.port.in.usecase;

import com.furnisight.order.application.promotion.port.in.dto.PromotionDto;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherCommand;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherResponse;

import java.util.List;
import java.util.UUID;

public interface VoucherUseCase {
    List<PromotionDto> getAvailableVouchers(UUID userId);
    ValidateVoucherResponse validateVoucher(ValidateVoucherCommand command);
    void saveVoucher(UUID userId, String promotionCode);
}
