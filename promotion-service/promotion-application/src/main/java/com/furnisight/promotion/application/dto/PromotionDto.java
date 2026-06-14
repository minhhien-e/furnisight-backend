package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PromotionDto {
    private String id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String voucherType;
    private String discountType;
    private Double discountValue;
    private Double maxDiscount;
    private Double minOrder;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private boolean active;
    private List<String> placements;
    private boolean saved;
    private boolean used;
    private String statusLabel;
    private long issuedCount;
}
