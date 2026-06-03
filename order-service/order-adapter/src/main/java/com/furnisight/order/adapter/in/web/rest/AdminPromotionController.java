package com.furnisight.order.adapter.in.web.rest;

import com.furnisight.order.adapter.in.web.dto.request.AdminSavePromotionRequest;
import com.furnisight.order.adapter.in.web.dto.response.AdminPromotionActionResponse;
import com.furnisight.order.adapter.in.web.dto.response.AdminPromotionListResponse;
import com.furnisight.order.application.promotion.port.in.dto.PromotionDto;
import com.furnisight.order.application.promotion.port.out.repository.PromotionRepository;
import com.furnisight.order.domain.entities.promotion.Promotion;
import com.furnisight.order.domain.enums.DiscountType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionRepository promotionRepository;

    @GetMapping
    public ResponseEntity<AdminPromotionListResponse> getPromotions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        String normalizedQuery = normalize(query);
        String normalizedStatus = normalize(status);
        return ResponseEntity.ok(new AdminPromotionListResponse(promotionRepository.findAll().stream()
                .filter(promotion -> matchesQuery(promotion, normalizedQuery))
                .filter(promotion -> matchesStatus(promotion, normalizedStatus))
                .sorted(Comparator.comparing(Promotion::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toDto)
                .toList()));
    }

    @PostMapping
    public ResponseEntity<AdminPromotionActionResponse> createPromotion(@RequestBody AdminSavePromotionRequest request) {
        String code = requireText(request.code(), "Thiếu mã voucher.").toUpperCase(Locale.ROOT);
        if (promotionRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại.");
        }

        promotionRepository.save(apply(Promotion.builder().id(UUID.randomUUID()).code(code).build(), request, code));
        return ResponseEntity.ok(new AdminPromotionActionResponse(true, "Voucher created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminPromotionActionResponse> updatePromotion(
            @PathVariable UUID id,
            @RequestBody AdminSavePromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
        String code = requireText(request.code(), "Thiếu mã voucher.").toUpperCase(Locale.ROOT);
        promotionRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã voucher đã tồn tại.");
                });

        promotionRepository.save(apply(promotion, request, code));
        return ResponseEntity.ok(new AdminPromotionActionResponse(true, "Voucher updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AdminPromotionActionResponse> deletePromotion(@PathVariable UUID id) {
        promotionRepository.deleteById(id);
        return ResponseEntity.ok(new AdminPromotionActionResponse(true, "Voucher deleted"));
    }

    private Promotion apply(Promotion promotion, AdminSavePromotionRequest request, String code) {
        promotion.setCode(code);
        promotion.setName(requireText(request.name(), "Thiếu tên voucher."));
        promotion.setDescription(defaultText(request.description()));
        promotion.setIcon(defaultText(request.icon(), "badgePercent"));
        promotion.setDiscountType(toDiscountType(request.discountType()));
        promotion.setDiscountValue(nonNegative(request.discountValue()));
        promotion.setMaxDiscount(nonNegativeOrNull(request.maxDiscount()));
        promotion.setMinOrder(nonNegativeOrNull(request.minOrder()));
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setActive(request.active() == null || request.active());
        return promotion;
    }

    private PromotionDto toDto(Promotion promotion) {
        return PromotionDto.builder()
                .id(promotion.getId().toString())
                .code(promotion.getCode())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .icon(promotion.getIcon())
                .discountType(promotion.getDiscountType().name())
                .discountValue(promotion.getDiscountValue())
                .maxDiscount(promotion.getMaxDiscount())
                .minOrder(promotion.getMinOrder())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .active(promotion.isActive())
                .build();
    }

    private boolean matchesQuery(Promotion promotion, String query) {
        return query == null
                || normalize(promotion.getCode()).contains(query)
                || normalize(promotion.getName()).contains(query);
    }

    private boolean matchesStatus(Promotion promotion, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        if ("active".equals(status)) {
            return promotion.isActive() && !isExpired(promotion);
        }
        if ("inactive".equals(status)) {
            return !promotion.isActive();
        }
        if ("expired".equals(status)) {
            return isExpired(promotion);
        }
        return true;
    }

    private boolean isExpired(Promotion promotion) {
        return promotion.getEndDate() != null && promotion.getEndDate().isBefore(LocalDateTime.now());
    }

    private DiscountType toDiscountType(String value) {
        try {
            return DiscountType.valueOf(defaultText(value, "PERCENT").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ.");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
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
        if (number < 0) {
            throw new IllegalArgumentException("Giá trị giảm không được âm.");
        }
        return number;
    }

    private Double nonNegativeOrNull(Double value) {
        if (value == null) {
            return null;
        }
        if (value < 0) {
            throw new IllegalArgumentException("Giá trị cấu hình không được âm.");
        }
        return value;
    }
}
