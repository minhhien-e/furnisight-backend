package com.furnisight.admin.service;

import com.furnisight.admin.catalog.CategoryDto;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.ProductStatsResponse;
import com.furnisight.admin.controller.dto.AdminStatsResponse;
import com.furnisight.admin.controller.dto.DashboardKpiResponse;
import com.furnisight.admin.integration.GrpcAdminCatalogClient;
import com.furnisight.admin.integration.GrpcAdminOrderClient;
import com.furnisight.admin.integration.GrpcAdminUserClient;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.user.AccountStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final GrpcAdminUserClient grpcAdminUserClient;
    private final GrpcAdminOrderClient grpcAdminOrderClient;
    private final GrpcAdminCatalogClient grpcAdminCatalogClient;

    public AdminStatsResponse getStats() {
        AccountStatsResponse userStats = grpcAdminUserClient.getAccountStats();
        OrderStatsResponse orderStats = grpcAdminOrderClient.getOrderStats();
        ProductStatsResponse productStats = grpcAdminCatalogClient.getProductStats();
        CategoryListResponse categories = grpcAdminCatalogClient.getCategories(null);

        List<CategoryDto> topCategories = categories.getCategoriesList().stream()
                .sorted(Comparator.comparingInt(CategoryDto::getProductCount).reversed())
                .limit(6)
                .toList();

        return new AdminStatsResponse(
                List.of(
                        new DashboardKpiResponse("users", "Người dùng", String.valueOf(userStats.getTotalUsers()), "",
                                userStats.getNewUsersThisMonth() + " mới tháng này", true, "blue", "users"),
                        new DashboardKpiResponse("orders", "Đơn hàng", String.valueOf(orderStats.getTotalOrders()), "",
                                orderStats.getOrdersToday() + " hôm nay", true, "red", "box"),
                        new DashboardKpiResponse("revenue", "Doanh thu", formatCurrency(orderStats.getTotalRevenue()), "",
                                formatCurrency(orderStats.getRevenueThisMonth()) + " tháng này", true, "gold", "trendingUp"),
                        new DashboardKpiResponse("products", "Sản phẩm", String.valueOf(productStats.getTotalProducts()), "",
                                productStats.getLowStockProducts() + " sắp hết", productStats.getLowStockProducts() == 0, "green", "armchair")),
                List.of("Tổng", "Hoạt động", "Bị khóa", "Mới tháng"),
                List.of(userStats.getTotalUsers(), userStats.getActiveUsers(), userStats.getBannedUsers(), userStats.getNewUsersThisMonth()),
                topCategories.stream().map(CategoryDto::getName).toList(),
                topCategories.stream().map(category -> (long) category.getProductCount()).toList());
    }

    private String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(value) + "đ";
    }
}
