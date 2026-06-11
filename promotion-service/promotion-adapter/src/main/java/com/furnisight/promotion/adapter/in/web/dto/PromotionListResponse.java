package com.furnisight.promotion.adapter.in.web.dto;

import com.furnisight.promotion.application.dto.PromotionDto;

import java.util.List;

public record PromotionListResponse(List<PromotionDto> items) {
}
