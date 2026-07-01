package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.RecommendVouchersUseCase;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class RecommendVouchersService implements RecommendVouchersUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional(readOnly = true)
    public RecommendVouchersResponse recommendVouchers(RecommendVouchersQuery query) {

        double subtotal = helper.amount(query.getCommand().getSubtotal());
        double shippingFee = helper.amount(query.getCommand().getShippingFee());
        List<UserVoucher> usable = query.getUserId() == null ? List.of()
                : userVoucherRepository.findUsableByUserId(query.getUserId(), LocalDateTime.now());
        List<Promotion> promotions = usable.stream().map(UserVoucher::getPromotion)
                .filter(p -> p != null && (p.getMinOrder() == null || subtotal >= p.getMinOrder()))
                .toList();
        String preferredCode = helper.normalizeCode(query.getCommand().getPreferredVoucherCode());
        Promotion preferred = promotions.stream()
                .filter(p -> preferredCode != null && preferredCode.equalsIgnoreCase(p.getCode()))
                .findFirst().orElse(null);

        Promotion shop = helper.chooseBest(promotions, false, preferred, subtotal, shippingFee);
        Promotion shipping = helper.chooseBest(promotions, true, preferred, subtotal, shippingFee);
        double shopDiscount = shop == null ? 0 : helper.calculateDiscount(shop, subtotal, shippingFee);
        double shippingDiscount = shipping == null ? 0 : helper.calculateDiscount(shipping, subtotal, shippingFee);
        return RecommendVouchersResponse.builder()
                .shopVoucher(shop == null ? null : helper.toDto(shop, helper.usableVoucher(usable, shop.getId())))
                .shopDiscount(shopDiscount)
                .shippingVoucher(shipping == null ? null : helper.toDto(shipping, helper.usableVoucher(usable, shipping.getId())))
                .shippingDiscount(shippingDiscount)
                .build();

    }
}
