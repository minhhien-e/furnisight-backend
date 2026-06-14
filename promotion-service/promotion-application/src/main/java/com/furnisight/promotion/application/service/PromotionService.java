package com.furnisight.promotion.application.service;

import com.furnisight.promotion.application.dto.PromotionDto;
import com.furnisight.promotion.application.dto.SavePromotionCommand;
import com.furnisight.promotion.application.dto.ValidateOrderVouchersCommand;
import com.furnisight.promotion.application.dto.ValidateOrderVouchersResponse;
import com.furnisight.promotion.application.dto.ValidateVoucherCommand;
import com.furnisight.promotion.application.dto.ValidateVoucherResponse;
import com.furnisight.promotion.application.dto.VoucherStatsResponse;
import com.furnisight.promotion.application.port.PromotionRepository;
import com.furnisight.promotion.application.port.UserVoucherRepository;
import com.furnisight.promotion.application.port.MarketingCampaignRepository;
import com.furnisight.promotion.application.port.PromotionComboRepository;
import com.furnisight.promotion.domain.entities.Promotion;
import com.furnisight.promotion.domain.entities.UserVoucher;
import com.furnisight.promotion.domain.enums.DiscountType;
import com.furnisight.promotion.domain.enums.VoucherType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;
    private final PromotionComboRepository promotionComboRepository;

    @Transactional(readOnly = true)
    public List<PromotionDto> getAvailableVouchers(UUID userId) {
        Map<UUID, UserVoucher> userVouchers = userVoucherMap(userId);
        List<Promotion> result = new ArrayList<>(promotionRepository.findAllActive().stream()
                .filter(p -> p.getVoucherType() == VoucherType.PUBLIC)
                .toList());
        if (userId != null) {
            for (UserVoucher uv : userVoucherRepository.findByUserId(userId)) {
                Promotion promotion = uv.getPromotion();
                if (promotion != null && promotion.isActive() && !uv.isUsed()
                        && result.stream().noneMatch(p -> p.getId().equals(promotion.getId()))) {
                    result.add(promotion);
                }
            }
        }
        return result.stream()
                .filter(this::isWithinWindow)
                .sorted(Comparator.comparing(Promotion::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(p -> toDto(p, userVouchers.get(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> getPublicVouchers(UUID userId, String placement) {
        Map<UUID, UserVoucher> userVouchers = userVoucherMap(userId);
        return promotionRepository.findAllActive().stream()
                .filter(p -> p.getVoucherType() == VoucherType.PUBLIC)
                .filter(this::isWithinWindow)
                .filter(p -> matchesPlacement(p.getPlacements(), placement))
                .sorted(Comparator.comparing(Promotion::getEndDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(p -> toDto(p, userVouchers.get(p.getId())))
                .toList();
    }

    public List<PromotionDto> getAdminVouchers(String query, String type, String status) {
        String normalizedQuery = normalize(query);
        String normalizedType = normalize(type);
        String normalizedStatus = normalize(status);
        return promotionRepository.findAll().stream()
                .filter(p -> matchesQuery(p, normalizedQuery))
                .filter(p -> matchesType(p, normalizedType))
                .filter(p -> matchesStatus(p, normalizedStatus))
                .sorted((left, right) -> {
                    LocalDateTime leftCreatedAt = left.getCreatedAt();
                    LocalDateTime rightCreatedAt = right.getCreatedAt();
                    if (leftCreatedAt == null && rightCreatedAt == null) return 0;
                    if (leftCreatedAt == null) return 1;
                    if (rightCreatedAt == null) return -1;
                    return rightCreatedAt.compareTo(leftCreatedAt);
                })
                .map(p -> toDto(p, null))
                .toList();
    }

    public VoucherStatsResponse getStats() {
        List<Promotion> promotions = promotionRepository.findAll();
        long active = promotions.stream().filter(p -> p.isActive() && isWithinWindow(p)).count();
        var campaigns = marketingCampaignRepository.findAll();
        var combos = promotionComboRepository.findAll();
        long runningCampaigns = campaigns.stream()
                .filter(c -> "RUNNING".equals(c.getStatus().name()) || "SCHEDULED".equals(c.getStatus().name()))
                .count();
        return VoucherStatsResponse.builder()
                .totalVouchers(promotions.size())
                .activeVouchers(active)
                .issuedCount(userVoucherRepository.countAll())
                .campaignCount(campaigns.size())
                .runningCampaignCount(runningCampaigns)
                .activeCombos(combos.stream().filter(c -> c.isActive() && (c.getStartDate() == null || !c.getStartDate().isAfter(LocalDateTime.now()))
                        && (c.getEndDate() == null || !c.getEndDate().isBefore(LocalDateTime.now()))).count())
                .comboUsedCount(combos.stream().mapToLong(c -> c.getUsedCount()).sum())
                .build();
    }

    public ValidateVoucherResponse validateVoucher(ValidateVoucherCommand command) {
        String code = normalizeCode(command.getCode());
        if (code == null) {
            return invalid("Thiếu mã voucher.");
        }

        var promotionOpt = promotionRepository.findByCode(code);
        if (promotionOpt.isEmpty()) {
            return invalid("Mã giảm giá không tồn tại.");
        }

        Promotion p = promotionOpt.get();
        String validationMessage = validatePromotion(p, command.getType(), amount(command.getSubtotal()));
        if (validationMessage != null) {
            return invalid(validationMessage);
        }

        double discount = calculateDiscount(p, amount(command.getSubtotal()), amount(command.getShippingFee()));
        return ValidateVoucherResponse.builder()
                .valid(true)
                .message("Áp dụng mã giảm giá thành công.")
                .voucher(toDto(p, null))
                .discount(discount)
                .build();
    }

    public ValidateOrderVouchersResponse validateOrderVouchers(ValidateOrderVouchersCommand command) {
        double subtotal = amount(command.getSubtotal());
        double shippingFee = amount(command.getShippingFee());
        double shopDiscount = 0.0;
        double shippingDiscount = 0.0;
        PromotionDto shopVoucher = null;
        PromotionDto shippingVoucher = null;

        if (hasText(command.getShopVoucherCode())) {
            ValidateVoucherResponse response = validateVoucher(ValidateVoucherCommand.builder()
                    .userId(command.getUserId())
                    .code(command.getShopVoucherCode())
                    .type("shop")
                    .subtotal(subtotal)
                    .shippingFee(shippingFee)
                    .build());
            if (!response.isValid()) {
                return invalidOrder(response.getMessage());
            }
            shopDiscount = response.getDiscount();
            shopVoucher = response.getVoucher();
        }

        if (hasText(command.getShippingVoucherCode())) {
            ValidateVoucherResponse response = validateVoucher(ValidateVoucherCommand.builder()
                    .userId(command.getUserId())
                    .code(command.getShippingVoucherCode())
                    .type("ship")
                    .subtotal(subtotal)
                    .shippingFee(shippingFee)
                    .build());
            if (!response.isValid()) {
                return invalidOrder(response.getMessage());
            }
            shippingDiscount = response.getDiscount();
            shippingVoucher = response.getVoucher();
        }

        return ValidateOrderVouchersResponse.builder()
                .valid(true)
                .message("Voucher hợp lệ.")
                .discountAmount(shopDiscount)
                .shippingDiscount(shippingDiscount)
                .shopVoucher(shopVoucher)
                .shippingVoucher(shippingVoucher)
                .build();
    }

    @Transactional
    public PromotionDto createVoucher(SavePromotionCommand command) {
        String code = requireCode(command.getCode());
        promotionRepository.findByCode(code).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã voucher đã tồn tại.");
        });
        return toDto(promotionRepository.save(apply(Promotion.builder().id(UUID.randomUUID()).build(), command, code)), null);
    }

    @Transactional
    public PromotionDto updateVoucher(UUID id, SavePromotionCommand command) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
        String code = requireCode(command.getCode());
        promotionRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã voucher đã tồn tại.");
                });
        return toDto(promotionRepository.save(apply(promotion, command, code)), null);
    }

    @Transactional
    public void deleteVoucher(UUID id) {
        promotionRepository.deleteById(id);
    }

    @Transactional
    public void saveVoucher(UUID userId, String code) {
        if (userId == null) {
            throw new IllegalArgumentException("Thiếu user.");
        }
        Promotion promotion = promotionRepository.findByCode(requireCode(code))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
        if (userVoucherRepository.findByUserIdAndPromotionId(userId, promotion.getId()).isPresent()) {
            return;
        }
        userVoucherRepository.save(UserVoucher.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .promotionId(promotion.getId())
                .used(false)
                .savedAt(LocalDateTime.now())
                .build());
    }

    private Promotion apply(Promotion promotion, SavePromotionCommand command, String code) {
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
        promotion.setPlacements(join(command.getPlacements()));
        return promotion;
    }

    private String validatePromotion(Promotion p, String requestType, double subtotal) {
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

    private double calculateDiscount(Promotion p, double subtotal, double shippingFee) {
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

    private PromotionDto toDto(Promotion p, UserVoucher userVoucher) {
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
                .placements(split(p.getPlacements()))
                .saved(userVoucher != null)
                .used(userVoucher != null && userVoucher.isUsed())
                .statusLabel(statusLabel(p))
                .issuedCount(userVoucherRepository.countByPromotionId(p.getId()))
                .build();
    }

    private Map<UUID, UserVoucher> userVoucherMap(UUID userId) {
        if (userId == null) return Map.of();
        Map<UUID, UserVoucher> result = new HashMap<>();
        for (UserVoucher userVoucher : userVoucherRepository.findByUserId(userId)) {
            if (userVoucher.getPromotionId() != null) {
                result.put(userVoucher.getPromotionId(), userVoucher);
            }
        }
        return result;
    }

    private boolean matchesPlacement(String placements, String placement) {
        if (!hasText(placement)) return true;
        List<String> values = split(placements);
        return values.isEmpty() || values.stream().anyMatch(value -> value.equalsIgnoreCase(placement.trim()));
    }

    private boolean isWithinWindow(Promotion p) {
        LocalDateTime now = LocalDateTime.now();
        return (p.getStartDate() == null || !p.getStartDate().isAfter(now))
                && (p.getEndDate() == null || !p.getEndDate().isBefore(now));
    }

    private String statusLabel(Promotion p) {
        if (!p.isActive()) return "Đang tắt";
        LocalDateTime now = LocalDateTime.now();
        if (p.getStartDate() != null && p.getStartDate().isAfter(now)) return "Sắp diễn ra";
        if (p.getEndDate() != null && p.getEndDate().isBefore(now)) return "Hết hạn";
        return "Đang bật";
    }

    private boolean matchesQuery(Promotion p, String query) {
        return query == null
                || normalize(p.getCode()).contains(query)
                || normalize(p.getName()).contains(query);
    }

    private boolean matchesType(Promotion p, String type) {
        return type == null || type.isBlank() || p.getVoucherType().name().toLowerCase(Locale.ROOT).equals(type);
    }

    private boolean matchesStatus(Promotion p, String status) {
        if (status == null || status.isBlank()) return true;
        return switch (status) {
            case "active" -> p.isActive() && isWithinWindow(p);
            case "inactive" -> !p.isActive();
            case "expired" -> p.getEndDate() != null && p.getEndDate().isBefore(LocalDateTime.now());
            default -> true;
        };
    }

    private ValidateVoucherResponse invalid(String message) {
        return ValidateVoucherResponse.builder().valid(false).message(message).discount(0.0).build();
    }

    private ValidateOrderVouchersResponse invalidOrder(String message) {
        return ValidateOrderVouchersResponse.builder()
                .valid(false)
                .message(message)
                .discountAmount(0.0)
                .shippingDiscount(0.0)
                .build();
    }

    private String requireCode(String value) {
        String code = normalizeCode(value);
        if (code == null) throw new IllegalArgumentException("Thiếu mã voucher.");
        return code;
    }

    private String normalizeCode(String value) {
        if (!hasText(value)) return null;
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String requireText(String value, String message) {
        if (!hasText(value)) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultText(String value, String fallback) {
        String text = defaultText(value);
        return text.isBlank() ? fallback : text;
    }

    private double nonNegative(Double value) {
        double number = value == null ? 0.0 : value;
        if (number < 0) throw new IllegalArgumentException("Giá trị giảm không được âm.");
        return number;
    }

    private Double nonNegativeOrNull(Double value) {
        if (value == null) return null;
        if (value < 0) throw new IllegalArgumentException("Giá trị cấu hình không được âm.");
        return value;
    }

    private double amount(Double value) {
        return value == null ? 0.0 : Math.max(0.0, value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        return String.join(",", values.stream().filter(this::hasText).map(String::trim).distinct().toList());
    }

    private List<String> split(String value) {
        if (!hasText(value)) return List.of();
        return java.util.Arrays.stream(value.split(",")).map(String::trim).filter(this::hasText).toList();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private <T extends Enum<T>> T parseEnum(String value, T fallback, Class<T> enumClass, String message) {
        if (!hasText(value)) return fallback;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(message);
        }
    }
}
