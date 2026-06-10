package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminRevenueResponse(
        List<DashboardKpiResponse> kpis,
        List<String> monthLabels,
        List<Double> monthData,
        List<AdminRevenueMonthlyRow> monthlyRows,
        String snapshotAt
) {}
