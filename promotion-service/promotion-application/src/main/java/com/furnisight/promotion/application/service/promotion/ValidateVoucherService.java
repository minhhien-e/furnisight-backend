package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.ValidateVoucherUseCase;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class ValidateVoucherService implements ValidateVoucherUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional(readOnly = true)
    public ValidateVoucherResponse validateVoucher(ValidateVoucherCommand query) {

        String code = helper.normalizeCode(query.getCode());
        if (code == null) {
            return helper.invalid("Thiếu mã voucher.");
        }

        var promotionOpt = promotionRepository.findByCode(code);
        if (promotionOpt.isEmpty()) {
            return helper.invalid("Mã giảm giá không tồn tại.");
        }

        Promotion p = promotionOpt.get();
        if (!helper.isOwnedUnusedVoucher(query.getUserId(), p.getId())) {
            return helper.invalid("Voucher không thuộc người dùng hoặc đã được sử dụng.");
        }
        String validationMessage = helper.validatePromotion(p, query.getType(), helper.amount(query.getSubtotal()));
        if (validationMessage != null) {
            return helper.invalid(validationMessage);
        }

        double discount = helper.calculateDiscount(p, helper.amount(query.getSubtotal()), helper.amount(query.getShippingFee()));
        return ValidateVoucherResponse.builder()
                .valid(true)
                .message("Áp dụng mã giảm giá thành công.")
                .voucher(helper.toDto(p, null))
                .discount(discount)
                .build();

    }
}
