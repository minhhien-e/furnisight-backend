package com.furnisight.admin.dashboard.web.dto.response;

import com.furnisight.admin.catalog.inventory.web.dto.response.LowStockItemResponse;
import com.furnisight.admin.order.web.dto.response.OrderResponse;
import com.furnisight.admin.shared.web.KpiResponse;

import java.util.List;

public record DashboardResponse(
        WelcomeResponse welcome,
        List<KpiResponse> kpis,
        ChartResponse revenueChart,
        ChartResponse orderChart,
        List<OrderResponse> recentOrders,
        List<LowStockItemResponse> lowStock,
        List<AlertResponse> alerts
) {
}
