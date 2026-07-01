package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.SaveUserVoucherUseCase;
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
public class SaveUserVoucherService implements SaveUserVoucherUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional
    public void saveVoucher(SaveUserVoucherQuery query) {

        if (query.getUserId() == null) {
            throw new IllegalArgumentException("Thiếu user.");
        }
        Promotion promotion = promotionRepository.findByCode(helper.requireCode(query.getCode()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
        if (promotion.getVoucherType() != VoucherType.PUBLIC || !promotion.isActive() || !helper.isWithinWindow(promotion)) {
            throw new IllegalArgumentException("Voucher không thể lưu tại thời điểm này.");
        }
        if (userVoucherRepository.findByUserIdAndPromotionId(query.getUserId(), promotion.getId()).isPresent()) {
            return;
        }
        userVoucherRepository.save(UserVoucher.builder()
                .id(UUID.randomUUID())
                .userId(query.getUserId())
                .promotionId(promotion.getId())
                .used(false)
                .savedAt(LocalDateTime.now())
                .build());

    }
}
