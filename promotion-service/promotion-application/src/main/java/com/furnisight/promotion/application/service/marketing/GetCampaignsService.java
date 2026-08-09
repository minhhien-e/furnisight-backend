package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.domain.common.PageResponse;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetCampaignsUseCase;
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
public class GetCampaignsService implements GetCampaignsUseCase {

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
    
    // Inject self for async if needed or just other usecases
    
    

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MarketingCampaignDto> getCampaigns(GetCampaignsQuery query) {
        String status = query.getStatus();
        List<MarketingCampaignDto> items = campaignRepository.findAll().stream()
                .filter(c -> helper.matches(c.getName(), query.getQuery()) || helper.matches(helper.voucherCode(c.getVoucherId()), query.getQuery()))
                .filter(c -> helper.matchesStatus(c.getStatus().name(), status))
                .sorted(Comparator.comparing(MarketingCampaign::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(helper::toCampaignDto)
                .toList();
        return helper.unpaged(items);
    }
}
