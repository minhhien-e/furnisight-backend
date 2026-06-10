package com.furnisight.admin.controller.dto;

public record AdminRevenueMonthlyRow(
        String month,
        long orders,
        String revenue,
        String mom,
        String momClass,
        String profit,
        String refund
) {}
