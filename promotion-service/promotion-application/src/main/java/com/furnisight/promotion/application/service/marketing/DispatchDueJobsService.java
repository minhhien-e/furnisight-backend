package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.DispatchDueJobsUseCase;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import com.furnisight.promotion.application.port.in.usecase.DispatchCampaignUseCase;
import com.furnisight.promotion.application.port.in.usecase.DispatchNotificationUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DispatchDueJobsService implements DispatchDueJobsUseCase {

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
    
    private final DispatchCampaignUseCase dispatchCampaignUseCase;
    private final DispatchNotificationUseCase dispatchNotificationUseCase;

    @Override
    
    public void dispatchDueJobs(DispatchDueJobsQuery query) {
        LocalDateTime now = LocalDateTime.now();
        campaignRepository.findDueScheduled(CampaignStatus.SCHEDULED, now).forEach(c -> dispatchCampaignUseCase.dispatchCampaign(new DispatchCampaignQuery(c)));
        notificationRepository.findDueScheduled(CampaignStatus.SCHEDULED, now).forEach(n -> dispatchNotificationUseCase.dispatchNotification(new DispatchNotificationQuery(n)));
    }
}
