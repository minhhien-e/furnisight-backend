package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminStatsResponse(
        List<DashboardKpiResponse> kpis,
        List<String> userLabels,
        List<Long> userData,
        List<String> categoryLabels,
        List<Long> categoryData
) {
}
