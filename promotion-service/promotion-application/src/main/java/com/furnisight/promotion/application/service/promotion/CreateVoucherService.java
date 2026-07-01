package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.CreateVoucherUseCase;
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
public class CreateVoucherService implements CreateVoucherUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional
    public PromotionDto createVoucher(SavePromotionCommand query) {

        String code = helper.requireCode(query.getCode());
        promotionRepository.findByCode(code).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã voucher đã tồn tại.");
        });
        return helper.toDto(promotionRepository.save(helper.apply(Promotion.builder().id(UUID.randomUUID()).build(), query, code)), null);

    }
}
