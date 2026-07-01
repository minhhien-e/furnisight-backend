package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.UpdateVoucherUseCase;
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
public class UpdateVoucherService implements UpdateVoucherUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional
    public PromotionDto updateVoucher(UpdateVoucherQuery query) {

        Promotion promotion = promotionRepository.findById(query.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
        String code = helper.requireCode(query.getCommand().getCode());
        promotionRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(query.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã voucher đã tồn tại.");
                });
        return helper.toDto(promotionRepository.save(helper.apply(promotion, query.getCommand(), code)), null);

    }
}
