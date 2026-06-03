package com.furnisight.order.adapter.in.web.dto.response;

import com.furnisight.order.application.promotion.port.in.dto.PromotionDto;

import java.util.List;

public record AdminPromotionListResponse(List<PromotionDto> items) {
}
