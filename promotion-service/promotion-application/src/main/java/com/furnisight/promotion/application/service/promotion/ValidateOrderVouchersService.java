package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.ValidateOrderVouchersUseCase;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;
import com.furnisight.promotion.application.port.in.usecase.ValidateVoucherUseCase;

@Service
@RequiredArgsConstructor
public class ValidateOrderVouchersService implements ValidateOrderVouchersUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    private final ValidateVoucherUseCase validateVoucherService;

    @Override
    @Transactional(readOnly = true)
    public ValidateOrderVouchersResponse validateOrderVouchers(ValidateOrderVouchersCommand query) {

        double subtotal = helper.amount(query.getSubtotal());
        double shippingFee = helper.amount(query.getShippingFee());
        double shopDiscount = 0.0;
        double shippingDiscount = 0.0;
        PromotionDto shopVoucher = null;
        PromotionDto shippingVoucher = null;

        if (helper.hasText(query.getShopVoucherCode())) {
            ValidateVoucherResponse response = validateVoucherService.validateVoucher(ValidateVoucherCommand.builder()
                    .userId(query.getUserId())
                    .code(query.getShopVoucherCode())
                    .type("shop")
                    .subtotal(subtotal)
                    .shippingFee(shippingFee)
                    .build());
            if (!response.isValid()) {
                return helper.invalidOrder(response.getMessage());
            }
            shopDiscount = response.getDiscount();
            shopVoucher = response.getVoucher();
        }

        if (helper.hasText(query.getShippingVoucherCode())) {
            ValidateVoucherResponse response = validateVoucherService.validateVoucher(ValidateVoucherCommand.builder()
                    .userId(query.getUserId())
                    .code(query.getShippingVoucherCode())
                    .type("ship")
                    .subtotal(subtotal)
                    .shippingFee(shippingFee)
                    .build());
            if (!response.isValid()) {
                return helper.invalidOrder(response.getMessage());
            }
            shippingDiscount = response.getDiscount();
            shippingVoucher = response.getVoucher();
        }

        return ValidateOrderVouchersResponse.builder()
                .valid(true)
                .message("Voucher hợp lệ.")
                .discountAmount(shopDiscount)
                .shippingDiscount(shippingDiscount)
                .shopVoucher(shopVoucher)
                .shippingVoucher(shippingVoucher)
                .build();

    }
}
