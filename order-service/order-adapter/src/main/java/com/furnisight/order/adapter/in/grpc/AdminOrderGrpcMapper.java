package com.furnisight.order.adapter.in.grpc;

import com.furnisight.admin.order.*;
import com.furnisight.order.application.order.port.in.dto.admin.AdminOrderPageResult;
import com.furnisight.order.application.order.port.in.dto.admin.OrderStatsResult;
import com.furnisight.order.application.order.port.in.dto.admin.RevenueSummaryResult;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.repository.order.TopSellingProductQuery;
import com.furnisight.order.domain.valueobjects.ProductSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminOrderGrpcMapper {

    public OrderPageResponse toOrderPageResponse(AdminOrderPageResult result) {
        return OrderPageResponse.newBuilder()
                .addAllOrders(result.getOrders().stream().map(this::toOrderDto).collect(Collectors.toList()))
                .setCurrentPage(result.getCurrentPage())
                .setTotalElements(result.getTotalElements())
                .setTotalPages(result.getTotalPages())
                .build();
    }

    public OrderStatsResponse toOrderStatsResponse(OrderStatsResult result) {
        OrderStatsResponse.Builder builder = OrderStatsResponse.newBuilder()
                .setTotalRevenue(result.getTotalRevenue())
                .setRevenueThisMonth(result.getRevenueThisMonth())
                .setTotalOrders(result.getTotalOrders())
                .setOrdersToday(result.getOrdersToday());

        if (result.getOrdersByStatus() != null) {
            result.getOrdersByStatus().forEach((status, count) -> 
                builder.addOrdersByStatus(OrderStatusCount.newBuilder().setStatus(status).setCount(count).build())
            );
        }

        if (result.getOrdersThisMonthByStatus() != null) {
            result.getOrdersThisMonthByStatus().forEach((status, count) -> 
                builder.addOrdersThisMonthByStatus(OrderStatusCount.newBuilder().setStatus(status).setCount(count).build())
            );
        }

        if (result.getRevenueChart() != null) {
            result.getRevenueChart().forEach(point -> 
                builder.addRevenueChart(ChartPoint.newBuilder().setLabel(point.getLabel()).setValue(point.getValue()).build())
            );
        }

        return builder.build();
    }

    public RevenueSummaryResponse toRevenueSummaryResponse(RevenueSummaryResult result) {
        RevenueSummaryResponse.Builder builder = RevenueSummaryResponse.newBuilder()
                .setTotalRevenue(result.getTotalRevenue())
                .setTotalOrders(result.getTotalOrders())
                .setRevenueThisMonth(result.getRevenueThisMonth())
                .setOrdersThisMonth(result.getOrdersThisMonth());

        if (result.getMonthly() != null) {
            result.getMonthly().forEach(monthly -> 
                builder.addMonthly(MonthlyRevenue.newBuilder()
                        .setYearMonth(monthly.getYearMonth())
                        .setRevenue(monthly.getRevenue())
                        .setOrderCount(monthly.getOrderCount())
                        .setMomChangePct(monthly.getMomChangePct())
                        .build())
            );
        }

        return builder.build();
    }

    public TopSellingProductsResponse toTopSellingProductsResponse(List<TopSellingProductQuery> queryResults) {
        TopSellingProductsResponse.Builder builder = TopSellingProductsResponse.newBuilder();
        for (TopSellingProductQuery product : queryResults) {
            TopProductDto.Builder productBuilder = TopProductDto.newBuilder();
            if (product.productId() != null) {
                productBuilder.setProductId(product.productId());
            }
            if (product.productName() != null) {
                productBuilder.setProductName(product.productName());
            }
            if (product.categoryName() != null) {
                productBuilder.setCategoryName(product.categoryName());
            }
            if (product.imageUrl() != null) {
                productBuilder.setImageUrl(product.imageUrl());
            }
            if (product.price() != null) {
                productBuilder.setPrice(product.price());
            }
            if (product.soldCount() != null) {
                productBuilder.setSoldCount(product.soldCount());
            }
            if (product.totalRevenue() != null) {
                productBuilder.setTotalRevenue(product.totalRevenue());
            }
            builder.addProducts(productBuilder.build());
        }
        return builder.build();
    }

    public OrderDto toOrderDto(Order order) {
        OrderDto.Builder builder = OrderDto.newBuilder();
        if (order.getId() != null) {
            builder.setId(order.getId().toString());
        }
        if (order.getOrderCode() != null) {
            builder.setOrderCode(order.getOrderCode());
        }
        if (order.getStatus() != null) {
            builder.setStatus(order.getStatus().name());
        }
        if (order.getTotalAmount() != null) {
            builder.setTotalAmount(order.getTotalAmount());
        }
        if (order.getCreatedAt() != null) {
            builder.setCreatedAt(order.getCreatedAt().toString());
        }
        if (order.getPaymentDetail() != null && order.getPaymentDetail().getPaymentMethod() != null) {
            builder.setPaymentMethod(order.getPaymentDetail().getPaymentMethod());
        }
        String firstProductImage = resolveFirstProductImage(order);
        if (firstProductImage != null) {
            builder.setFirstProductImage(firstProductImage);
        }
        String customer = resolveCustomer(order);
        if (customer != null) {
            builder.setCustomer(customer);
        }
        if (order.getItems() != null) {
            builder.setItemCount(resolveItemCount(order));
        }
        if (order.getTrackingCode() != null) {
            builder.setTrackingCode(order.getTrackingCode());
        }
        return builder.build();
    }

    private String resolveFirstProductImage(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return null;
        }
        ProductSnapshot productSnapshot = order.getItems().get(0).getProductSnapshot();
        return productSnapshot == null ? null : productSnapshot.getImageUrl();
    }

    private int resolveItemCount(Order order) {
        if (order.getItems() == null) {
            return 0;
        }
        return order.getItems().stream().mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum();
    }

    private String resolveCustomer(Order order) {
        if (order.getShippingDetail() == null) {
            return null;
        }
        return order.getShippingDetail().getShippingAddressName();
    }
}
