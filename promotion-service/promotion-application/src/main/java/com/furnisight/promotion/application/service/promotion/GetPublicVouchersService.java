package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.domain.common.PageResponse;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetPublicVouchersUseCase;
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
public class GetPublicVouchersService implements GetPublicVouchersUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromotionDto> getPublicVouchers(GetPublicVouchersQuery query) {

        int safePage = Math.max(0, query.getPage() == null ? 0 : query.getPage());
        int safeSize = Math.max(1, Math.min(24, query.getSize() == null ? 6 : query.getSize()));
        String normalizedFilter = query.getFilter() == null || query.getFilter().isBlank() ? "all" : query.getFilter().trim().toLowerCase(Locale.ROOT);
        if (!List.of("all", "freeship", "expiring").contains(normalizedFilter)) {
            throw new IllegalArgumentException("Filter voucher không hợp lệ.");
        }
        Map<UUID, UserVoucher> userVouchers = helper.userVoucherMap(query.getUserId());
        LocalDateTime now = LocalDateTime.now();
        var result = promotionRepository.findPublicActivePage(now,
                "expiring".equals(normalizedFilter) ? now.plusDays(7) : null,
                "freeship".equals(normalizedFilter), safePage, safeSize);
        return new PageResponse<>(result.items().stream()
                .map(p -> helper.toDto(p, userVouchers.get(p.getId())))
                .toList(), result.totalPages(), result.totalElements(), safePage, safeSize);

    }
}
