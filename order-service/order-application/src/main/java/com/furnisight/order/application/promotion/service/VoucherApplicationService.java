package com.furnisight.order.application.promotion.service;

import com.furnisight.order.application.promotion.port.in.dto.PromotionDto;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherCommand;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherResponse;
import com.furnisight.order.application.promotion.port.in.usecase.VoucherUseCase;
import com.furnisight.order.application.promotion.port.out.repository.PromotionRepository;
import com.furnisight.order.application.promotion.port.out.repository.UserVoucherRepository;
import com.furnisight.order.domain.entities.promotion.Promotion;
import com.furnisight.order.domain.entities.promotion.UserVoucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherApplicationService implements VoucherUseCase {

    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Override
    public List<PromotionDto> getAvailableVouchers(UUID userId) {
        // For simplicity, return all active global promotions + user saved ones (deduplicated)
        List<Promotion> activePromotions = promotionRepository.findAllActive();
        List<UserVoucher> userVouchers = userVoucherRepository.findByUserId(userId);
        
        // Add any user-specific promotions that are not globally active (if applicable)
        List<Promotion> result = new ArrayList<>(activePromotions);
        for (UserVoucher uv : userVouchers) {
            if (uv.getPromotion() != null && uv.getPromotion().isActive() && !uv.isUsed()) {
                if (result.stream().noneMatch(p -> p.getId().equals(uv.getPromotion().getId()))) {
                    result.add(uv.getPromotion());
                }
            }
        }
        
        return result.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ValidateVoucherResponse validateVoucher(ValidateVoucherCommand command) {
        Optional<Promotion> promotionOpt = promotionRepository.findByCode(command.getCode());
        
        if (promotionOpt.isEmpty()) {
            return ValidateVoucherResponse.builder()
                    .valid(false)
                    .message("Mã giảm giá không tồn tại.")
                    .build();
        }
        
        Promotion p = promotionOpt.get();
        if (!p.isActive()) {
            return ValidateVoucherResponse.builder()
                    .valid(false)
                    .message("Mã giảm giá đã hết hạn hoặc không còn hiệu lực.")
                    .build();
        }
        
        if (p.getEndDate() != null && p.getEndDate().isBefore(LocalDateTime.now())) {
            return ValidateVoucherResponse.builder()
                    .valid(false)
                    .message("Mã giảm giá đã hết hạn.")
                    .build();
        }
        
        if (p.getMinOrder() != null && command.getSubtotal() < p.getMinOrder()) {
            return ValidateVoucherResponse.builder()
                    .valid(false)
                    .message("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này.")
                    .build();
        }
        
        // Calculate discount
        double discount = 0.0;
        switch (p.getDiscountType()) {
            case FIXED:
                discount = p.getDiscountValue();
                break;
            case PERCENT:
                discount = command.getSubtotal() * (p.getDiscountValue() / 100.0);
                if (p.getMaxDiscount() != null && discount > p.getMaxDiscount()) {
                    discount = p.getMaxDiscount();
                }
                break;
            case SHIPPING_CAP:
                // For shipping cap, it usually caps the shipping fee, but here we just return the cap amount.
                // It should be applied to shippingFee specifically.
                discount = p.getDiscountValue();
                break;
        }
        
        return ValidateVoucherResponse.builder()
                .valid(true)
                .message("Áp dụng mã giảm giá thành công.")
                .voucher(mapToDto(p))
                .discount(discount)
                .build();
    }

    @Override
    public void saveVoucher(UUID userId, String promotionCode) {
        Promotion promotion = promotionRepository.findByCode(promotionCode)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found"));
                
        UserVoucher uv = UserVoucher.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .promotionId(promotion.getId())
                .isUsed(false)
                .savedAt(LocalDateTime.now())
                .build();
                
        userVoucherRepository.save(uv);
    }
    
    private PromotionDto mapToDto(Promotion p) {
        return PromotionDto.builder()
                .id(p.getId().toString())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .icon(p.getIcon())
                .discountType(p.getDiscountType().name())
                .discountValue(p.getDiscountValue())
                .maxDiscount(p.getMaxDiscount())
                .minOrder(p.getMinOrder())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .active(p.isActive())
                .build();
    }
}
