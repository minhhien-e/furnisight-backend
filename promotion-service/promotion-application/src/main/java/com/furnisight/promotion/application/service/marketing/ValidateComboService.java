package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.ValidateComboUseCase;
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
public class ValidateComboService implements ValidateComboUseCase {

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
    public ValidateComboResponse validateCombo(ValidateComboCommand query) {
        UUID comboId = helper.parseUuid(query.getComboId());
        if (comboId == null) {
            return helper.invalidCombo(query.getComboId(), "Missing combo id");
        }
        PromotionCombo combo = comboRepository.findById(comboId).orElse(null);
        if (combo == null) {
            return helper.invalidCombo(query.getComboId(), "Combo not found");
        }
        if (!combo.isActive()) {
            return helper.invalidCombo(combo.getId().toString(), "Combo inactive");
        }
        LocalDateTime now = LocalDateTime.now();
        if (combo.getStartDate() != null && combo.getStartDate().isAfter(now)) {
            return helper.invalidCombo(combo.getId().toString(), "Combo not started");
        }
        if (combo.getEndDate() != null && combo.getEndDate().isBefore(now)) {
            return helper.invalidCombo(combo.getId().toString(), "Combo expired");
        }
        List<PromotionComboItem> requiredItems = comboItemRepository.findByComboId(combo.getId());
        if (requiredItems.isEmpty()) {
            return helper.invalidCombo(combo.getId().toString(), "Combo has no items");
        }
        List<ValidateComboCommand.Item> requestedItems = query.getItems() == null ? List.of() : query.getItems();
        boolean hasRequiredItems = requiredItems.stream().allMatch(item -> {
            int requestedQuantity = requestedItems.stream()
                    .filter(requested -> item.getProductId().equals(requested.getProductId()))
                    .filter(requested -> !helper.hasText(item.getVariantId()) || item.getVariantId().equals(requested.getVariantId()))
                    .mapToInt(requested -> Math.max(0, requested.getQuantity() == null ? 0 : requested.getQuantity()))
                    .sum();
            return requestedQuantity >= Math.max(1, item.getQuantity());
        });
        if (!hasRequiredItems) {
            return helper.invalidCombo(combo.getId().toString(), "Cart does not contain all combo items");
        }
        Map<String, CatalogStockPort.StockItem> stock = helper.lookupStock(requiredItems);
        boolean hasStock = requiredItems.stream().allMatch(item -> {
            var live = stock.get(helper.stockKey(item.getProductId(), item.getVariantId()));
            return live != null && live.stockQuantity() != null
                    && live.stockQuantity() >= Math.max(1, item.getQuantity());
        });
        if (!hasStock) {
            return helper.invalidCombo(combo.getId().toString(), "Combo items are out of stock");
        }
        return ValidateComboResponse.builder()
                .valid(true)
                .comboId(combo.getId().toString())
                .comboName(combo.getName())
                .originalAmount(combo.getOriginalAmount())
                .finalAmount(combo.getFinalAmount())
                .comboDiscount(combo.getSavedAmount())
                .message("Combo valid")
                .build();
    }
}
