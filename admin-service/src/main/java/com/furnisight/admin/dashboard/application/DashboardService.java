package com.furnisight.admin.dashboard.application;

import com.furnisight.admin.dashboard.web.dto.response.DashboardResponse;
import com.furnisight.admin.dashboard.web.dto.response.AlertResponse;
import com.furnisight.admin.dashboard.web.dto.response.ChartResponse;
import com.furnisight.admin.shared.web.KpiResponse;
import com.furnisight.admin.shared.web.KpiType;
import com.furnisight.admin.dashboard.web.dto.response.AlertType;
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

import java.util.List;

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
                orderStats.getRevenueThisMonth(),
                orderStats.getOrdersToday(),
                userStats.getTotalUsers());

        List<KpiResponse> kpis = List.of(
                new KpiResponse(KpiType.USERS, userStats.getTotalUsers(), (double) userStats.getNewUsersThisMonth()),
                new KpiResponse(KpiType.REVENUE, orderStats.getRevenueThisMonth(), orderStats.getTotalRevenue()),
                new KpiResponse(KpiType.ORDERS, orderStats.getTotalOrders(), (double) orderStats.getOrdersToday()),
                new KpiResponse(KpiType.PRODUCTS, productStats.getTotalProducts(), (double) productStats.getLowStockProducts()));

        List<String> validStatuses = List.of("CONFIRMED", "PAID", "SHIPPING", "DELIVERED", "CANCELLED");

        return new DashboardResponse(
                welcome,
                kpis,
                new ChartResponse(
                        orderStats.getRevenueChartList().stream().map(point -> point.getLabel()).toList(),
                        orderStats.getRevenueChartList().stream().map(point -> (double) point.getValue()).toList()),
                new ChartResponse(
                        orderStats.getOrdersThisMonthByStatusList().stream()
                                .filter(s -> validStatuses.contains(s.getStatus()))
                                .map(OrderStatusCount::getStatus).toList(),
                        orderStats.getOrdersThisMonthByStatusList().stream()
                                .filter(s -> validStatuses.contains(s.getStatus()))
                                .map(point -> (double) point.getCount()).toList()),
                orderService.getRecentOrders(5),
                inventoryService.getLowStockProducts(5),
                buildAlerts(orderStats, productStats, userStats));
    }

    private List<AlertResponse> buildAlerts(OrderStatsResponse orderStats, ProductStatsResponse productStats, AccountStatsResponse userStats) {
        return List.of(
                new AlertResponse(AlertType.ORDERS_TODAY, orderStats.getOrdersToday()),
                new AlertResponse(AlertType.LOW_STOCK, productStats.getLowStockProducts()),
                new AlertResponse(AlertType.NEW_USERS, userStats.getNewUsersThisMonth())
        );
    }
}
