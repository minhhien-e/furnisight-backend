package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminInventoryResponse(
        List<DashboardKpiResponse> kpis,
        List<AdminInventoryItemResponse> items
) {
}
