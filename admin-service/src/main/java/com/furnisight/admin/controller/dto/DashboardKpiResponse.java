package com.furnisight.admin.controller.dto;

public record DashboardKpiResponse(
        String key,
        String label,
        String value,
        String suffix,
        String change,
        boolean up,
        String tone,
        String icon
) {
}
