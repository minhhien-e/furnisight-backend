package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.domain.common.PageResponse;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetPublicCombosUseCase;
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
public class GetPublicCombosService implements GetPublicCombosUseCase {

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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MarketingComboDto> getPublicCombos(GetPublicCombosQuery query) {
        int safeSize = Math.max(1, Math.min(24, query.getSize() == null ? 6 : query.getSize()));
        int safePage = query.getPage() == null ? 0 : Math.max(0, query.getPage());
        LocalDateTime now = LocalDateTime.now();
        if (!query.isAvailableOnly()) {
            var dbPage = comboRepository.findActivePage(now, safePage, safeSize, query.getSort());
            return new PageResponse<>(helper.enrichStock(dbPage.items()), dbPage.totalPages(), dbPage.totalElements(), safePage, safeSize);
        }

        int wantedFrom = safePage * safeSize;
        int availableCount = 0;
        int scanPage = 0;
        List<MarketingComboDto> requested = new ArrayList<>();
        while (true) {
            var dbPage = comboRepository.findActivePage(now, scanPage, 24, query.getSort());
            for (MarketingComboDto combo : helper.enrichStock(dbPage.items())) {
                if (!combo.isAvailable()) continue;
                if (availableCount >= wantedFrom && requested.size() < safeSize) requested.add(combo);
                availableCount++;
            }
            scanPage++;
            if (scanPage >= dbPage.totalPages()) break;
        }
        return new PageResponse<>(requested, (int) Math.ceil((double) availableCount / safeSize),
                availableCount, safePage, safeSize);
    }
}
