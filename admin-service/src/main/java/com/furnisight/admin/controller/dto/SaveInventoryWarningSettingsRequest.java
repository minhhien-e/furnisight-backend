package com.furnisight.admin.controller.dto;

import java.util.Map;

public record SaveInventoryWarningSettingsRequest(
        Integer defaultThreshold,
        Map<String, Integer> variantThresholds
) {
}
