package com.furnisight.promotion.application.dto;

import java.util.List;

public record MarketingListResponse<T>(List<T> items, long totalElements) {
    public MarketingListResponse {
        items = items == null ? List.of() : items;
    }
}
