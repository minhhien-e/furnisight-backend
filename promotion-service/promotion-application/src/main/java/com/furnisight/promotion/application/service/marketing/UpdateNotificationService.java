package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.UpdateNotificationUseCase;
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
public class UpdateNotificationService implements UpdateNotificationUseCase {

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
    public MarketingNotificationDto updateNotification(UpdateNotificationQuery query) {
        MarketingNotification notification = notificationRepository.findById(query.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        helper.applyNotification(notification, query.getCommand());
        notification = notificationRepository.save(notification);
        if (notification.getSendType() == MarketingSendType.NOW && notification.getDispatchedAt() == null) {
            dispatchNotificationUseCase.dispatchNotification(new DispatchNotificationQuery(notification));
        }
        return helper.toNotificationDto(notificationRepository.findById(query.getId()).orElse(notification));
    }
}
