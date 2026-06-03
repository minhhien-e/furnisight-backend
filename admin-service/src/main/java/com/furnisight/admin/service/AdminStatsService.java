package com.furnisight.admin.service;

import com.furnisight.admin.catalog.CategoryDto;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.ProductStatsResponse;
import com.furnisight.admin.controller.dto.AdminStatsCategoryMetricResponse;
import com.furnisight.admin.controller.dto.AdminStatsOrderMetricsResponse;
import com.furnisight.admin.controller.dto.AdminStatsProductMetricsResponse;
import com.furnisight.admin.controller.dto.AdminStatsResponse;
import com.furnisight.admin.controller.dto.AdminStatsUserMetricsResponse;
import com.furnisight.admin.integration.GrpcAdminCatalogClient;
import com.furnisight.admin.integration.GrpcAdminOrderClient;
import com.furnisight.admin.integration.GrpcAdminUserClient;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.user.AccountStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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
                new AdminStatsUserMetricsResponse(
                        userStats.getTotalUsers(),
                        userStats.getActiveUsers(),
                        userStats.getBannedUsers(),
                        userStats.getNewUsersThisMonth()),
                new AdminStatsOrderMetricsResponse(
                        orderStats.getTotalOrders(),
                        orderStats.getOrdersToday(),
                        orderStats.getTotalRevenue(),
                        orderStats.getRevenueThisMonth()),
                new AdminStatsProductMetricsResponse(
                        productStats.getTotalProducts(),
                        productStats.getLowStockProducts()),
                topCategories.stream()
                        .map(this::toCategoryMetric)
                        .toList());
    }

    private AdminStatsCategoryMetricResponse toCategoryMetric(CategoryDto category) {
        return new AdminStatsCategoryMetricResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getProductCount());
    }
}
