package com.furnisight.promotion.application.service.promotion;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class PromotionHelper {

    private final UserVoucherRepository userVoucherRepository;

public Promotion apply(Promotion promotion, SavePromotionCommand command, String code) {
        promotion.setCode(code);
        promotion.setName(requireText(command.getName(), "Thiếu tên voucher."));
        promotion.setDescription(defaultText(command.getDescription()));
        promotion.setIcon(defaultText(command.getIcon(), "badgePercent"));
        promotion.setVoucherType(parseEnum(command.getVoucherType(), VoucherType.PUBLIC, VoucherType.class, "Loại voucher không hợp lệ."));
        promotion.setDiscountType(parseEnum(command.getDiscountType(), DiscountType.PERCENT, DiscountType.class, "Loại giảm giá không hợp lệ."));
        promotion.setDiscountValue(nonNegative(command.getDiscountValue()));
        promotion.setMaxDiscount(nonNegativeOrNull(command.getMaxDiscount()));
        promotion.setMinOrder(nonNegativeOrNull(command.getMinOrder()));
        promotion.setStartDate(command.getStartDate());
        promotion.setEndDate(command.getEndDate());
        promotion.setActive(command.getActive() == null || command.getActive());
        return promotion;
    }

    public String validatePromotion(Promotion p, String requestType, double subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (!p.isActive()) return "Mã giảm giá đã tắt hoặc không còn hiệu lực.";
        if (p.getStartDate() != null && p.getStartDate().isAfter(now)) return "Mã giảm giá chưa đến thời gian áp dụng.";
        if (p.getEndDate() != null && p.getEndDate().isBefore(now)) return "Mã giảm giá đã hết hạn.";
        if (p.getMinOrder() != null && subtotal < p.getMinOrder()) return "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này.";

        String type = normalize(requestType);
        if ("ship".equals(type) && p.getDiscountType() != DiscountType.SHIPPING_CAP) {
            return "Mã này không áp dụng cho vận chuyển.";
        }
        if (!"ship".equals(type) && p.getDiscountType() == DiscountType.SHIPPING_CAP) {
            return "Mã vận chuyển không áp dụng cho voucher shop.";
        }
        return null;
    }

    public double calculateDiscount(Promotion p, double subtotal, double shippingFee) {
        double discount = switch (p.getDiscountType()) {
            case FIXED -> p.getDiscountValue();
            case PERCENT -> {
                double raw = subtotal * (p.getDiscountValue() / 100.0);
                yield p.getMaxDiscount() != null ? Math.min(raw, p.getMaxDiscount()) : raw;
            }
            case SHIPPING_CAP -> Math.min(shippingFee, p.getDiscountValue());
        };
        double target = p.getDiscountType() == DiscountType.SHIPPING_CAP ? shippingFee : subtotal;
        return Math.max(0.0, Math.min(discount, target));
    }

    public PromotionDto toDto(Promotion p, UserVoucher userVoucher) {
        return PromotionDto.builder()
                .id(p.getId().toString())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .icon(p.getIcon())
                .voucherType(p.getVoucherType() == null ? VoucherType.PUBLIC.name() : p.getVoucherType().name())
                .discountType(p.getDiscountType().name())
                .discountValue(p.getDiscountValue())
                .maxDiscount(p.getMaxDiscount())
                .minOrder(p.getMinOrder())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .createdAt(p.getCreatedAt())
                .active(p.isActive())
                .saved(userVoucher != null)
                .used(userVoucher != null && userVoucher.isUsed())
                .statusLabel(statusLabel(p))
                .issuedCount(userVoucherRepository.countByPromotionId(p.getId()))
                .build();
    }

    public Map<UUID, UserVoucher> userVoucherMap(UUID userId) {
        if (userId == null) return Map.of();
        Map<UUID, UserVoucher> result = new HashMap<>();
        for (UserVoucher userVoucher : userVoucherRepository.findByUserId(userId)) {
            if (userVoucher.getPromotionId() != null) {
                result.put(userVoucher.getPromotionId(), userVoucher);
            }
        }
        return result;
    }

    public boolean isOwnedUnusedVoucher(UUID userId, UUID promotionId) {
        return userId != null && userVoucherRepository.findByUserIdAndPromotionId(userId, promotionId)
                .filter(uv -> !uv.isUsed()).isPresent();
    }

    public Promotion chooseBest(List<Promotion> promotions, boolean shipping, Promotion preferred,
                                 double subtotal, double shippingFee) {
        if (preferred != null && (preferred.getDiscountType() == DiscountType.SHIPPING_CAP) == shipping) return preferred;
        return promotions.stream()
                .filter(p -> (p.getDiscountType() == DiscountType.SHIPPING_CAP) == shipping)
                .max(Comparator.comparingDouble((Promotion p) -> calculateDiscount(p, subtotal, shippingFee))
                        .thenComparing(Promotion::getEndDate, Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(Promotion::getCode, Comparator.nullsLast(Comparator.reverseOrder())))
                .orElse(null);
    }

    public UserVoucher usableVoucher(List<UserVoucher> vouchers, UUID promotionId) {
        return vouchers.stream().filter(uv -> promotionId.equals(uv.getPromotionId())).findFirst().orElse(null);
    }

    public boolean isWithinWindow(Promotion p) {
        LocalDateTime now = LocalDateTime.now();
        return (p.getStartDate() == null || !p.getStartDate().isAfter(now))
                && (p.getEndDate() == null || !p.getEndDate().isBefore(now));
    }

    public String statusLabel(Promotion p) {
        if (!p.isActive()) return "Đang tắt";
        LocalDateTime now = LocalDateTime.now();
        if (p.getStartDate() != null && p.getStartDate().isAfter(now)) return "Sắp diễn ra";
        if (p.getEndDate() != null && p.getEndDate().isBefore(now)) return "Hết hạn";
        return "Đang bật";
    }

    public boolean matchesQuery(Promotion p, String query) {
        return query == null
                || normalize(p.getCode()).contains(query)
                || normalize(p.getName()).contains(query);
    }

    public boolean matchesType(Promotion p, String type) {
        return type == null || type.isBlank() || p.getVoucherType().name().toLowerCase(Locale.ROOT).equals(type);
    }

    public boolean matchesStatus(Promotion p, String status) {
        if (status == null || status.isBlank()) return true;
        return switch (status) {
            case "active" -> p.isActive() && isWithinWindow(p);
            case "inactive" -> !p.isActive();
            case "expired" -> p.getEndDate() != null && p.getEndDate().isBefore(LocalDateTime.now());
            default -> true;
        };
    }

    public ValidateVoucherResponse invalid(String message) {
        return ValidateVoucherResponse.builder().valid(false).message(message).discount(0.0).build();
    }

    public ValidateOrderVouchersResponse invalidOrder(String message) {
        return ValidateOrderVouchersResponse.builder()
                .valid(false)
                .message(message)
                .discountAmount(0.0)
                .shippingDiscount(0.0)
                .build();
    }

    public String requireCode(String value) {
        String code = normalizeCode(value);
        if (code == null) throw new IllegalArgumentException("Thiếu mã voucher.");
        return code;
    }

    public String normalizeCode(String value) {
        if (!hasText(value)) return null;
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public String requireText(String value, String message) {
        if (!hasText(value)) throw new IllegalArgumentException(message);
        return value.trim();
    }

    public String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    public String defaultText(String value, String fallback) {
        String text = defaultText(value);
        return text.isBlank() ? fallback : text;
    }

    public double nonNegative(Double value) {
        double number = value == null ? 0.0 : value;
        if (number < 0) throw new IllegalArgumentException("Giá trị giảm không được âm.");
        return number;
    }

    public Double nonNegativeOrNull(Double value) {
        if (value == null) return null;
        if (value < 0) throw new IllegalArgumentException("Giá trị cấu hình không được âm.");
        return value;
    }

    public double amount(Double value) {
        return value == null ? 0.0 : Math.max(0.0, value);
    }

    public boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    public <T extends Enum<T>> T parseEnum(String value, T fallback, Class<T> enumClass, String message) {
        if (!hasText(value)) return fallback;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(message);
        }
    }

}
