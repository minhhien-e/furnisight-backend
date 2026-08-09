package com.furnisight.promotion.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private Integer pageSize;

    public PageResponse(List<T> items, int totalPages, long totalElements, int currentPage) {
        this.items = items;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = null;
    }
}
