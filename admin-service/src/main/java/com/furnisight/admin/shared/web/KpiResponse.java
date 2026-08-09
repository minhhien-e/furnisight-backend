package com.furnisight.admin.shared.web;

public record KpiResponse(
        KpiType type,
        double value,
        Double changeValue
) {
}
