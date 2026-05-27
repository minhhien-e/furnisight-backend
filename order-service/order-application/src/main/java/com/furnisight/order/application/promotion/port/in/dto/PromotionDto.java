package com.furnisight.order.application.promotion.port.in.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PromotionDto {
    private String id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String discountType;
    private Double discountValue;
    private Double maxDiscount;
    private Double minOrder;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}
