package com.furnisight.promotion.application.service;

import com.furnisight.promotion.application.dto.ValidateVoucherCommand;
import com.furnisight.promotion.application.port.MarketingCampaignRepository;
import com.furnisight.promotion.application.port.PromotionRepository;
import com.furnisight.promotion.application.port.PromotionComboRepository;
import com.furnisight.promotion.application.port.UserVoucherRepository;
import com.furnisight.promotion.domain.entities.MarketingCampaign;
import com.furnisight.promotion.domain.entities.PromotionCombo;
import com.furnisight.promotion.domain.entities.Promotion;
import com.furnisight.promotion.domain.entities.UserVoucher;
import com.furnisight.promotion.domain.enums.CampaignStatus;
import com.furnisight.promotion.domain.enums.DiscountType;
import com.furnisight.promotion.domain.enums.VoucherType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionServiceTest {

    private final InMemoryPromotionRepository promotionRepository = new InMemoryPromotionRepository();
    private final InMemoryUserVoucherRepository userVoucherRepository = new InMemoryUserVoucherRepository();
    private final PromotionService service = new PromotionService(
            promotionRepository,
            userVoucherRepository,
            new EmptyMarketingCampaignRepository(),
            new EmptyPromotionComboRepository()
    );

    @Test
    void percentVoucherUsesMaxDiscountCap() {
        promotionRepository.save(voucher("SAVE50", DiscountType.PERCENT, 50.0, 120.0, 0.0));

        var response = validate("SAVE50", "shop", 1000.0, 0.0);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscount()).isEqualTo(120.0);
    }

    @Test
    void fixedVoucherCannotExceedSubtotal() {
        promotionRepository.save(voucher("FIXED", DiscountType.FIXED, 500.0, null, 0.0));

        var response = validate("FIXED", "shop", 300.0, 0.0);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscount()).isEqualTo(300.0);
    }

    @Test
    void shippingVoucherIsClampedByShippingFee() {
        promotionRepository.save(voucher("SHIP", DiscountType.SHIPPING_CAP, 50.0, null, 0.0));

        var response = validate("SHIP", "ship", 1000.0, 35.0);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscount()).isEqualTo(35.0);
    }

    @Test
    void rejectsWrongVoucherTypeForShopAndShipping() {
        promotionRepository.save(voucher("SHOP", DiscountType.PERCENT, 10.0, null, 0.0));
        promotionRepository.save(voucher("SHIP", DiscountType.SHIPPING_CAP, 30.0, null, 0.0));

        assertThat(validate("SHOP", "ship", 1000.0, 40.0).isValid()).isFalse();
        assertThat(validate("SHIP", "shop", 1000.0, 40.0).isValid()).isFalse();
    }

    @Test
    void rejectsInactiveExpiredNotStartedAndMinOrder() {
        Promotion inactive = voucher("OFF", DiscountType.PERCENT, 10.0, null, 0.0);
        inactive.setActive(false);
        promotionRepository.save(inactive);

        Promotion future = voucher("FUTURE", DiscountType.PERCENT, 10.0, null, 0.0);
        future.setStartDate(LocalDateTime.now().plusDays(1));
        promotionRepository.save(future);

        Promotion expired = voucher("EXPIRED", DiscountType.PERCENT, 10.0, null, 0.0);
        expired.setEndDate(LocalDateTime.now().minusDays(1));
        promotionRepository.save(expired);

        promotionRepository.save(voucher("MIN", DiscountType.PERCENT, 10.0, null, 500.0));

        assertThat(validate("OFF", "shop", 1000.0, 0.0).isValid()).isFalse();
        assertThat(validate("FUTURE", "shop", 1000.0, 0.0).isValid()).isFalse();
        assertThat(validate("EXPIRED", "shop", 1000.0, 0.0).isValid()).isFalse();
        assertThat(validate("MIN", "shop", 300.0, 0.0).isValid()).isFalse();
    }

    private com.furnisight.promotion.application.dto.ValidateVoucherResponse validate(
            String code,
            String type,
            double subtotal,
            double shippingFee
    ) {
        return service.validateVoucher(ValidateVoucherCommand.builder()
                .code(code)
                .type(type)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .build());
    }

    private Promotion voucher(
            String code,
            DiscountType discountType,
            double discountValue,
            Double maxDiscount,
            double minOrder
    ) {
        return Promotion.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name(code)
                .description("")
                .icon("badgePercent")
                .voucherType(VoucherType.PUBLIC)
                .discountType(discountType)
                .discountValue(discountValue)
                .maxDiscount(maxDiscount)
                .minOrder(minOrder)
                .active(true)
                .build();
    }

    private static final class InMemoryPromotionRepository implements PromotionRepository {
        private final List<Promotion> promotions = new ArrayList<>();

        @Override
        public Optional<Promotion> findById(UUID id) {
            return promotions.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Promotion> findByCode(String code) {
            return promotions.stream().filter(p -> p.getCode().equalsIgnoreCase(code)).findFirst();
        }

        @Override
        public List<Promotion> findAll() {
            return List.copyOf(promotions);
        }

        @Override
        public List<Promotion> findAllActive() {
            return promotions.stream().filter(Promotion::isActive).toList();
        }

        @Override
        public Promotion save(Promotion promotion) {
            promotions.removeIf(p -> p.getId().equals(promotion.getId()));
            promotions.add(promotion);
            return promotion;
        }

        @Override
        public void deleteById(UUID id) {
            promotions.removeIf(p -> p.getId().equals(id));
        }
    }

    private static final class InMemoryUserVoucherRepository implements UserVoucherRepository {
        private final List<UserVoucher> userVouchers = new ArrayList<>();

        @Override
        public List<UserVoucher> findByUserId(UUID userId) {
            return userVouchers.stream().filter(v -> v.getUserId().equals(userId)).toList();
        }

        @Override
        public Optional<UserVoucher> findByUserIdAndPromotionId(UUID userId, UUID promotionId) {
            return userVouchers.stream()
                    .filter(v -> v.getUserId().equals(userId))
                    .filter(v -> v.getPromotionId().equals(promotionId))
                    .findFirst();
        }

        @Override
        public long countByPromotionId(UUID promotionId) {
            return userVouchers.stream().filter(v -> v.getPromotionId().equals(promotionId)).count();
        }

        @Override
        public long countAll() {
            return userVouchers.size();
        }

        @Override
        public UserVoucher save(UserVoucher userVoucher) {
            userVouchers.add(userVoucher);
            return userVoucher;
        }
    }

    private static final class EmptyMarketingCampaignRepository implements MarketingCampaignRepository {
        @Override
        public List<MarketingCampaign> findAll() {
            return List.of();
        }

        @Override
        public Optional<MarketingCampaign> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public MarketingCampaign save(MarketingCampaign campaign) {
            return campaign;
        }

        @Override
        public void deleteById(UUID id) {
        }

        @Override
        public List<MarketingCampaign> findDueScheduled(CampaignStatus status, LocalDateTime now) {
            return List.of();
        }
    }

    private static final class EmptyPromotionComboRepository implements PromotionComboRepository {
        @Override
        public List<PromotionCombo> findAll() {
            return List.of();
        }

        @Override
        public List<PromotionCombo> findActive() {
            return List.of();
        }

        @Override
        public Optional<PromotionCombo> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public PromotionCombo save(PromotionCombo combo) {
            return combo;
        }

        @Override
        public void deleteById(UUID id) {
        }
    }
}
