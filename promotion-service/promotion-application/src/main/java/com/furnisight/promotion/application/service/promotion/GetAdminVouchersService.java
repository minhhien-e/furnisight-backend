package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.GetAdminVouchersUseCase;
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
public class GetAdminVouchersService implements GetAdminVouchersUseCase {

    private final PromotionHelper helper;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;
    

    @Override
    @Transactional(readOnly = true)
    public List<PromotionDto> getAdminVouchers(GetAdminVouchersQuery query) {

        String normalizedQuery = helper.normalize(query.getQuery());
        String normalizedType = helper.normalize(query.getType());
        String normalizedStatus = helper.normalize(query.getStatus());
        return promotionRepository.findAll().stream()
                .filter(p -> helper.matchesQuery(p, normalizedQuery))
                .filter(p -> helper.matchesType(p, normalizedType))
                .filter(p -> helper.matchesStatus(p, normalizedStatus))
                .sorted((left, right) -> {
                    LocalDateTime leftCreatedAt = left.getCreatedAt();
                    LocalDateTime rightCreatedAt = right.getCreatedAt();
                    if (leftCreatedAt == null && rightCreatedAt == null) return 0;
                    if (leftCreatedAt == null) return 1;
                    if (rightCreatedAt == null) return -1;
                    return rightCreatedAt.compareTo(leftCreatedAt);
                })
                .map(p -> helper.toDto(p, null))
                .toList();

    }
}
