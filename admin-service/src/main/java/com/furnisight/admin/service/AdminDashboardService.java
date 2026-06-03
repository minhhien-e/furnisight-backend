package com.furnisight.admin.service;

import com.furnisight.admin.controller.dto.AdminDashboardResponse;
import com.furnisight.admin.controller.dto.DashboardAlertResponse;
import com.furnisight.admin.controller.dto.DashboardChartResponse;
import com.furnisight.admin.controller.dto.DashboardKpiResponse;
import com.furnisight.admin.controller.dto.DashboardWelcomeResponse;
import com.furnisight.admin.integration.GrpcAdminUserClient;
import com.furnisight.admin.integration.GrpcAdminOrderClient;
import com.furnisight.admin.integration.GrpcAdminCatalogClient;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.order.OrderStatusCount;
import com.furnisight.admin.catalog.ProductStatsResponse;
import com.furnisight.admin.user.AccountStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final GrpcAdminUserClient grpcAdminUserClient;
    private final GrpcAdminOrderClient grpcAdminOrderClient;
    private final GrpcAdminCatalogClient grpcAdminCatalogClient;
    private final AdminOrderService adminOrderService;
    private final AdminCatalogService adminCatalogService;
    private final AdminInventorySettingsService adminInventorySettingsService;

    public AdminDashboardResponse getDashboardData() {
        AccountStatsResponse userStats = grpcAdminUserClient.getAccountStats();
        OrderStatsResponse orderStats = grpcAdminOrderClient.getOrderStats();
        ProductStatsResponse productStats = grpcAdminCatalogClient.getProductStats();

        DashboardWelcomeResponse welcome = new DashboardWelcomeResponse(
                formatCurrency(orderStats.getRevenueThisMonth()),
                orderStats.getOrdersToday(),
                userStats.getTotalUsers());

        List<DashboardKpiResponse> kpis = List.of(
                new DashboardKpiResponse("users", "Người dùng", String.valueOf(userStats.getTotalUsers()), "",
                        "+" + userStats.getNewUsersThisMonth() + " tháng này", true, "blue", "users"),
                new DashboardKpiResponse("revenue", "Doanh thu tháng", formatCurrency(orderStats.getRevenueThisMonth()), "",
                        formatCurrency(orderStats.getTotalRevenue()) + " tổng", true, "gold", "trendingUp"),
                new DashboardKpiResponse("orders", "Đơn hàng", String.valueOf(orderStats.getTotalOrders()), "",
                        orderStats.getOrdersToday() + " hôm nay", true, "red", "box"),
                new DashboardKpiResponse("products", "Sản phẩm", String.valueOf(productStats.getTotalProducts()), "",
                        productStats.getLowStockProducts() + " sắp hết", productStats.getLowStockProducts() == 0, "green", "armchair"));

        return new AdminDashboardResponse(
                welcome,
                kpis,
                new DashboardChartResponse(
                        orderStats.getRevenueChartList().stream().map(point -> point.getLabel()).toList(),
                        orderStats.getRevenueChartList().stream().map(point -> point.getValue() / 1_000_000D).toList()),
                new DashboardChartResponse(
                        orderStats.getOrdersThisMonthByStatusList().stream().map(OrderStatusCount::getLabel).toList(),
                        orderStats.getOrdersThisMonthByStatusList().stream().map(point -> (double) point.getCount()).toList()),
                adminOrderService.getRecentOrders(5),
                adminCatalogService.getLowStockProducts(5, adminInventorySettingsService.defaultThreshold()),
                buildAlerts(orderStats, productStats));
    }

    private List<DashboardAlertResponse> buildAlerts(OrderStatsResponse orderStats, ProductStatsResponse productStats) {
        return List.of(
                new DashboardAlertResponse("Đơn hôm nay", orderStats.getOrdersToday() + " đơn cần theo dõi", "box", "gold"),
                new DashboardAlertResponse("Sắp hết hàng", productStats.getLowStockProducts() + " sản phẩm dưới ngưỡng", "alert", "red"),
                new DashboardAlertResponse("Người dùng mới", "Theo dõi tăng trưởng tài khoản trong tháng", "users", "blue"));
    }

    private String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(value) + "đ";
    }
}
