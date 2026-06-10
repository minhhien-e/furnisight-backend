package com.furnisight.admin.revenue.web.dto.response;

import com.furnisight.admin.shared.web.KpiResponse;

import java.util.List;

public record RevenueResponse(
        List<KpiResponse> kpis,
        List<String> monthLabels,
        List<Double> monthData,
        List<RevenueMonthlyItemResponse> monthlyRows,
        List<TopProductItemResponse> topProducts,
        String snapshotAt
) {}
