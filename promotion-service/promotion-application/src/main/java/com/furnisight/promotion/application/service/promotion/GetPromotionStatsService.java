package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetPromotionStatsUseCase;
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
public class GetPromotionStatsService implements GetPromotionStatsUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional(readOnly = true)
    public VoucherStatsResponse getStats(GetPromotionStatsQuery query) {

        List<Promotion> promotions = promotionRepository.findAll();
        long active = promotions.stream().filter(p -> p.isActive() && helper.isWithinWindow(p)).count();
        var campaigns = marketingCampaignRepository.findAll();
        var combos = promotionComboRepository.findAll();
        long runningCampaigns = campaigns.stream()
                .filter(c -> "RUNNING".equals(c.getStatus().name()) || "SCHEDULED".equals(c.getStatus().name()))
                .count();
        return VoucherStatsResponse.builder()
                .totalVouchers(promotions.size())
                .activeVouchers(active)
                .issuedCount(userVoucherRepository.countAll())
                .campaignCount(campaigns.size())
                .runningCampaignCount(runningCampaigns)
                .activeCombos(combos.stream().filter(c -> c.isActive() && (c.getStartDate() == null || !c.getStartDate().isAfter(LocalDateTime.now()))
                        && (c.getEndDate() == null || !c.getEndDate().isBefore(LocalDateTime.now()))).count())
                .comboUsedCount(combos.stream().mapToLong(c -> c.getUsedCount()).sum())
                .build();

    }
}
