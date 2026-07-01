package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.domain.common.PageResponse;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetCombosUseCase;
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
public class GetCombosService implements GetCombosUseCase {

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
    public PageResponse<MarketingComboDto> getCombos(GetCombosQuery query) {
        List<MarketingComboDto> items = comboRepository.findAll().stream()
                .filter(c -> helper.matches(c.getName(), query.getQuery()))
                .filter(c -> helper.matchesStatus(helper.comboStatus(c), query.getStatus()))
                .sorted(Comparator.comparing(PromotionCombo::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(helper::toComboDto)
                .toList();
        return helper.unpaged(items);
    }
}
