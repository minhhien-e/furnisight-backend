package com.furnisight.admin.shared.web;

public record KpiResponse(
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
