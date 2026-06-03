package com.furnisight.admin.controller.dto;

import java.util.Map;

public record InventoryWarningSettingsResponse(
        int defaultThreshold,
        Map<String, Integer> variantThresholds
) {
}
