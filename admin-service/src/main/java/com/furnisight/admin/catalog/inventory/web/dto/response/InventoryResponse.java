package com.furnisight.admin.catalog.inventory.web.dto.response;

import com.furnisight.admin.shared.web.KpiResponse;

import java.util.List;

public record InventoryResponse(
        List<KpiResponse> kpis,
        List<InventoryItemResponse> items
) {
}
