package com.furnisight.order.application.order.port.in.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryResult {
    private double totalRevenue;
    private long totalOrders;
    private double revenueThisMonth;
    private long ordersThisMonth;
    private List<MonthlyRevenueResult> monthly;
}
