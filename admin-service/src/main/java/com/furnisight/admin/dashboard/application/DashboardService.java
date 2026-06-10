package com.furnisight.admin.dashboard.application;

import com.furnisight.admin.dashboard.web.dto.response.DashboardResponse;
import com.furnisight.admin.dashboard.web.dto.response.AlertResponse;
import com.furnisight.admin.dashboard.web.dto.response.ChartResponse;
import com.furnisight.admin.shared.web.KpiResponse;
import com.furnisight.admin.dashboard.web.dto.response.WelcomeResponse;
import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.catalog.inventory.application.InventoryService;
import com.furnisight.admin.order.application.OrderService;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
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
public class DashboardService {

    private final AdminUserGrpcClient userClient;
    private final AdminOrderGrpcClient orderClient;
    private final AdminCatalogGrpcClient catalogClient;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public DashboardResponse getDashboardData() {
        AccountStatsResponse userStats = userClient.getAccountStats();
        OrderStatsResponse orderStats = orderClient.getOrderStats();
        ProductStatsResponse productStats = catalogClient.getProductStats();

        WelcomeResponse welcome = new WelcomeResponse(
                formatCurrency(orderStats.getRevenueThisMonth()),
                orderStats.getOrdersToday(),
                userStats.getTotalUsers());

        List<KpiResponse> kpis = List.of(
                new KpiResponse("users", "Người dùng", String.valueOf(userStats.getTotalUsers()), "",
                        "+" + userStats.getNewUsersThisMonth() + " tháng này", true, "blue", "users"),
                new KpiResponse("revenue", "Doanh thu tháng", formatCurrency(orderStats.getRevenueThisMonth()), "",
                        formatCurrency(orderStats.getTotalRevenue()) + " tổng", true, "gold", "trendingUp"),
                new KpiResponse("orders", "Đơn hàng", String.valueOf(orderStats.getTotalOrders()), "",
                        orderStats.getOrdersToday() + " hôm nay", true, "red", "box"),
                new KpiResponse("products", "Sản phẩm", String.valueOf(productStats.getTotalProducts()), "",
                        productStats.getLowStockProducts() + " sắp hết", productStats.getLowStockProducts() == 0, "green", "armchair"));

        return new DashboardResponse(
                welcome,
                kpis,
                new ChartResponse(
                        orderStats.getRevenueChartList().stream().map(point -> point.getLabel()).toList(),
                        orderStats.getRevenueChartList().stream().map(point -> point.getValue() / 1_000_000D).toList()),
                new ChartResponse(
                        orderStats.getOrdersThisMonthByStatusList().stream().map(OrderStatusCount::getLabel).toList(),
                        orderStats.getOrdersThisMonthByStatusList().stream().map(point -> (double) point.getCount()).toList()),
                orderService.getRecentOrders(5),
                inventoryService.getLowStockProducts(5, 5),
                buildAlerts(orderStats, productStats));
    }

    private List<AlertResponse> buildAlerts(OrderStatsResponse orderStats, ProductStatsResponse productStats) {
        return List.of(
                new AlertResponse("Đơn hôm nay", orderStats.getOrdersToday() + " đơn cần theo dõi", "box", "gold"),
                new AlertResponse("Sắp hết hàng", productStats.getLowStockProducts() + " sản phẩm dưới ngưỡng", "alert", "red"),
                new AlertResponse("Người dùng mới", "Theo dõi tăng trưởng tài khoản trong tháng", "users", "blue"));
    }

    private String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(value) + "đ";
    }
}
