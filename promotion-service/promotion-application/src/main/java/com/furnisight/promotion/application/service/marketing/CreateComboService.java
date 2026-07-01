package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.CreateComboUseCase;
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
public class CreateComboService implements CreateComboUseCase {

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
    @Transactional
    public MarketingComboDto createCombo(SaveMarketingComboCommand query) {
        PromotionCombo combo = PromotionCombo.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();
        helper.applyCombo(combo, query);
        combo = comboRepository.save(combo);
        helper.saveComboItems(combo, query);
        helper.recalculateCombo(combo.getId());
        return helper.toComboDto(comboRepository.findById(combo.getId()).orElse(combo));
    }
}
