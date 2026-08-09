package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.port.in.query.GetAvailableVouchersQuery;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetAvailableVouchersUseCase;
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
public class GetAvailableVouchersService implements GetAvailableVouchersUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional(readOnly = true)
    public List<PromotionDto> getAvailableVouchers(GetAvailableVouchersQuery query) {

        if (query.getUserId() == null) return List.of();
        return userVoucherRepository.findUsableByUserId(query.getUserId(), LocalDateTime.now()).stream()
                .map(uv -> helper.toDto(uv.getPromotion(), uv))
                .toList();

    }
}
