package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.domain.common.PageResponse;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetNotificationsUseCase;
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
public class GetNotificationsService implements GetNotificationsUseCase {

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
    public PageResponse<MarketingNotificationDto> getNotifications(GetNotificationsQuery query) {
        String status = query.getStatus();
        List<MarketingNotificationDto> items = notificationRepository.findAll().stream()
                .filter(n -> helper.matches(n.getTitle(), query.getQuery()))
                .filter(n -> helper.matchesStatus(n.getStatus().name(), query.getStatus()))
                .sorted(Comparator.comparing(MarketingNotification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(helper::toNotificationDto)
                .toList();
        return helper.unpaged(items);
    }
}
