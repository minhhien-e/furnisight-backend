package com.furnisight.promotion.application.service;

import com.furnisight.promotion.application.dto.PageResponse;
import com.furnisight.promotion.application.port.CatalogStockPort;
import com.furnisight.promotion.application.port.PromotionComboItemRepository;
import com.furnisight.promotion.application.port.PromotionComboRepository;
import com.furnisight.promotion.domain.entities.PromotionCombo;
import com.furnisight.promotion.domain.entities.PromotionComboItem;
import com.furnisight.promotion.domain.enums.ComboDiscountType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MarketingServiceTest {
    @Test
    void availableOnlyOmitsComboWhenAnyRequiredVariantLacksStock() {
        PromotionCombo combo = PromotionCombo.builder().id(UUID.randomUUID()).name("Living room")
                .discountType(ComboDiscountType.FIXED_AMOUNT).active(true).savedAmount(100).build();
        PromotionComboItem item = PromotionComboItem.builder().id(UUID.randomUUID()).comboId(combo.getId())
                .productId("product-1").variantId("variant-1").quantity(2).build();
        PromotionComboRepository combos = new PromotionComboRepository() {
            public List<PromotionCombo> findAll() { return List.of(combo); }
            public List<PromotionCombo> findActive() { return List.of(combo); }
            public PageResponse<PromotionCombo> findActivePage(LocalDateTime now, int page, int size, String sort) {
                return new PageResponse<>(List.of(combo), 1, 1, page, size);
            }
            public Optional<PromotionCombo> findById(UUID id) { return Optional.of(combo); }
            public PromotionCombo save(PromotionCombo value) { return value; }
            public void deleteById(UUID id) {}
        };
        PromotionComboItemRepository items = new PromotionComboItemRepository() {
            public List<PromotionComboItem> findByComboId(UUID comboId) { return List.of(item); }
            public List<PromotionComboItem> findByComboIds(Collection<UUID> comboIds) { return List.of(item); }
            public PromotionComboItem save(PromotionComboItem value) { return value; }
            public void deleteByComboId(UUID comboId) {}
        };
        CatalogStockPort catalog = ignored -> Map.of("product-1::variant-1",
                new CatalogStockPort.StockItem("product-1", "variant-1", 1));
        MarketingService service = new MarketingService(null, null, combos, items, null,
                null, null, null, null, catalog);

        var result = service.getPublicCombos(true, "save-desc", 0, 3);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}
