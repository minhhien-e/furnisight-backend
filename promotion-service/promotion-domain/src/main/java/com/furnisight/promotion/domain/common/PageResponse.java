package com.furnisight.promotion.domain.common;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int totalPages,
        long totalElements,
        int currentPage,
        Integer pageSize
) {
    public PageResponse(List<T> items, int totalPages, long totalElements, int currentPage) {
        this(items, totalPages, totalElements, currentPage, null);
    }
}
