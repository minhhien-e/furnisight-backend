package com.furnisight.admin.stats.application;

import com.furnisight.admin.catalog.CategoryDto;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.ProductStatsResponse;
import com.furnisight.admin.stats.web.dto.response.CategoryMetricResponse;
import com.furnisight.admin.stats.web.dto.response.OrderMetricsResponse;
import com.furnisight.admin.stats.web.dto.response.ProductMetricsResponse;
import com.furnisight.admin.stats.web.dto.response.ReviewSentimentMetricResponse;
import com.furnisight.admin.stats.web.dto.response.StatsResponse;
import com.furnisight.admin.stats.web.dto.response.TopNegativeProductMetricResponse;
import com.furnisight.admin.stats.web.dto.response.UserMetricsResponse;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import com.furnisight.admin.review.infrastructure.grpc.AdminReviewGrpcClient;
import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.review.ReviewSentimentStatsResponse;
import com.furnisight.admin.user.AccountStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final AdminUserGrpcClient userClient;
    private final AdminOrderGrpcClient orderClient;
    private final AdminCatalogGrpcClient catalogClient;
    private final AdminReviewGrpcClient reviewClient;

    public StatsResponse getStats() {
        AccountStatsResponse userStats = userClient.getAccountStats();
        OrderStatsResponse orderStats = orderClient.getOrderStats();
        ProductStatsResponse productStats = catalogClient.getProductStats();
        CategoryListResponse categories = catalogClient.getCategories(null);
        ReviewSentimentStatsResponse reviewStats = reviewClient.getReviewSentimentStats();

        List<CategoryDto> topCategories = categories.getCategoriesList().stream()
                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().trim().isEmpty())
                .sorted(Comparator.comparingInt(CategoryDto::getProductCount).reversed())
                .limit(5)
                .toList();

        return new StatsResponse(
                new UserMetricsResponse(
                        userStats.getTotalUsers(),
                        userStats.getActiveUsers(),
                        userStats.getBannedUsers(),
                        userStats.getNewUsersThisMonth()),
                new OrderMetricsResponse(
                        orderStats.getTotalOrders(),
                        orderStats.getOrdersToday(),
                        orderStats.getTotalRevenue(),
                        orderStats.getRevenueThisMonth()),
                new ProductMetricsResponse(
                        productStats.getTotalProducts(),
                        productStats.getLowStockProducts()),
                topCategories.stream()
                        .map(this::toCategoryMetric)
                        .toList(),
                new ReviewSentimentMetricResponse(
                        reviewStats.getTotalReviews(),
                        reviewStats.getAnalyzedReviews(),
                        reviewStats.getPendingReviews(),
                        reviewStats.getFailedReviews(),
                        reviewStats.getPositiveCount(),
                        reviewStats.getNeutralCount(),
                        reviewStats.getNegativeCount(),
                        reviewStats.getTopNegativeProductsList().stream()
                                .map(product -> {
                                    String productName = product.getProductName();
                                    try {
                                        ProductDto dto = catalogClient.getProductDetail(product.getProductId());
                                        if (dto != null && dto.getName() != null && !dto.getName().isBlank()) {
                                            productName = dto.getName();
                                        }
                                    } catch (Exception e) {
                                        // Ignore if product not found or catalog service is down
                                    }
                                    return new TopNegativeProductMetricResponse(
                                        product.getProductId(),
                                        productName,
                                        product.getNegativeCount(),
                                        product.getNegativeRatio(),
                                        product.getVisibleReviewCount(),
                                        product.getAverageRating()
                                    );
                                })
                                .toList()
                ));
    }

    private CategoryMetricResponse toCategoryMetric(CategoryDto category) {
        return new CategoryMetricResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getProductCount());
    }
}
