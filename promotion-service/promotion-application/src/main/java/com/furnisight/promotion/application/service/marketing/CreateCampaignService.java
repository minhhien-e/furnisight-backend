package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.CreateCampaignUseCase;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CreateCampaignService implements CreateCampaignUseCase {

    private final MarketingHelper helper;
    private final MarketingCampaignRepository campaignRepository;
    private final MarketingNotificationRepository notificationRepository;
    private final PromotionComboRepository comboRepository;
    private final PromotionComboItemRepository comboItemRepository;
    private final MarketingDispatchLogRepository dispatchLogRepository;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingNotificationGateway notificationGateway;
    private final MarketingTargetGateway targetGateway;
    private final CatalogStockPort catalogStockPort;
    
    private final com.furnisight.promotion.application.port.in.usecase.DispatchCampaignUseCase dispatchCampaignUseCase;
    
    

    @Override
    @Transactional
    public MarketingCampaignDto createCampaign(SaveMarketingCampaignCommand query) {
        MarketingCampaign campaign = MarketingCampaign.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();
        helper.applyCampaign(campaign, query);
        campaign = campaignRepository.save(campaign);
        if (campaign.getScheduleType() == MarketingSendType.NOW) {
            dispatchCampaignUseCase.dispatchCampaign(new DispatchCampaignQuery(campaign));
        }
        return helper.toCampaignDto(campaignRepository.findById(campaign.getId()).orElse(campaign));
    }
}
