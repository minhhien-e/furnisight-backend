package com.furnisight.admin.controller.dto;

public record AdminStatsCategoryMetricResponse(
        String id,
        String name,
        String slug,
        long productCount
) {
}
