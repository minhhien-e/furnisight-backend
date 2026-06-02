package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminDashboardResponse(
        DashboardWelcomeResponse welcome,
        List<DashboardKpiResponse> kpis,
        DashboardChartResponse revenueChart,
        DashboardChartResponse orderChart,
        List<DashboardRecentOrderResponse> recentOrders,
        List<DashboardLowStockResponse> lowStock,
        List<DashboardAlertResponse> alerts
) {
}
