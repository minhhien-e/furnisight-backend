package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.CreateNotificationUseCase;
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
public class CreateNotificationService implements CreateNotificationUseCase {

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
    
    private final com.furnisight.promotion.application.port.in.usecase.DispatchNotificationUseCase dispatchNotificationUseCase;
    
    

    @Override
    @Transactional
    public MarketingNotificationDto createNotification(SaveMarketingNotificationCommand query) {
        MarketingNotification notification = MarketingNotification.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();
        helper.applyNotification(notification, query);
        notification = notificationRepository.save(notification);
        if (notification.getSendType() == MarketingSendType.NOW) {
            dispatchNotificationUseCase.dispatchNotification(new DispatchNotificationQuery(notification));
        }
        return helper.toNotificationDto(notificationRepository.findById(notification.getId()).orElse(notification));
    }
}
