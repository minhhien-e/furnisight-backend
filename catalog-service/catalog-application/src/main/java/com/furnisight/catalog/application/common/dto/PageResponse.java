package com.furnisight.catalog.application.common.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int totalPages,
        long totalElements,
        int currentPage,
        Integer pageSize
) {
}
