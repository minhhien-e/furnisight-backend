package com.furnisight.order.application.order.port.in.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsResult {
    private double totalRevenue;
    private double revenueThisMonth;
    private long totalOrders;
    private long ordersToday;
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> ordersThisMonthByStatus;
    private List<ChartPointResult> revenueChart;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPointResult {
        private String label;
        private double value;
    }
}
