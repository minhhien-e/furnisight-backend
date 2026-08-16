package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.usecase.GetOrderStatsUseCase;
import com.furnisight.order.application.order.port.in.query.GetOrderStatsQuery;
import com.furnisight.order.application.order.port.in.dto.admin.OrderStatsResult;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.OrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOrderStatsService implements GetOrderStatsUseCase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderStatsResult getOrderStats(GetOrderStatsQuery query) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime nextMonthStart = firstDayOfMonth.plusMonths(1).atStartOfDay();

        if (query.getStartDate() != null && !query.getStartDate().isBlank() &&
            query.getEndDate() != null && !query.getEndDate().isBlank()) {
            try {
                LocalDate start = LocalDate.parse(query.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate end = LocalDate.parse(query.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                monthStart = start.atStartOfDay();
                nextMonthStart = end.plusDays(1).atStartOfDay();
                todayStart = monthStart;
                tomorrowStart = nextMonthStart;
            } catch (Exception e) {
                // Ignore parse errors and fallback to defaults
            }
        }

        Map<String, Long> ordersByStatus = new HashMap<>();
        Map<String, Long> ordersThisMonthByStatus = new HashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
            ordersThisMonthByStatus.put(status.name(), orderRepository.countByStatusCreatedAtBetween(status, monthStart, nextMonthStart));
        }

        List<OrderStatsResult.ChartPointResult> revenueChart = new ArrayList<>();
        if (query.getStartDate() != null && !query.getStartDate().isBlank() &&
            query.getEndDate() != null && !query.getEndDate().isBlank()) {
            try {
                LocalDate start = LocalDate.parse(query.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate end = LocalDate.parse(query.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                
                if (days <= 31) {
                    for (int i = 0; i <= days; i++) {
                        LocalDate bucket = start.plusDays(i);
                        LocalDateTime bucketStart = bucket.atStartOfDay();
                        LocalDateTime bucketEnd = bucket.plusDays(1).atStartOfDay();
                        revenueChart.add(OrderStatsResult.ChartPointResult.builder()
                                .label(bucket.format(DATE_FORMAT))
                                .value(orderRepository.sumTotalAmountCreatedAtBetween(bucketStart, bucketEnd))
                                .build());
                    }
                } else {
                    long months = java.time.temporal.ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1));
                    for (int i = 0; i <= months; i++) {
                        LocalDate bucket = start.plusMonths(i).withDayOfMonth(1);
                        LocalDateTime bucketStart = bucket.atStartOfDay();
                        LocalDateTime bucketEnd = bucket.plusMonths(1).atStartOfDay();
                        revenueChart.add(OrderStatsResult.ChartPointResult.builder()
                                .label(bucket.format(DATE_FORMAT))
                                .value(orderRepository.sumTotalAmountCreatedAtBetween(bucketStart, bucketEnd))
                                .build());
                    }
                }
            } catch (Exception e) {
                // Ignore parse errors and fallback
            }
        }
        
        if (revenueChart.isEmpty()) {
            for (int i = 5; i >= 0; i--) {
                LocalDate bucket = today.minusMonths(i).withDayOfMonth(1);
                LocalDateTime bucketStart = bucket.atStartOfDay();
                LocalDateTime bucketEnd = bucket.plusMonths(1).atStartOfDay();
                revenueChart.add(OrderStatsResult.ChartPointResult.builder()
                        .label(bucket.format(DATE_FORMAT))
                        .value(orderRepository.sumTotalAmountCreatedAtBetween(bucketStart, bucketEnd))
                        .build());
            }
        }

        return OrderStatsResult.builder()
                .totalRevenue(orderRepository.sumTotalAmount())
                .revenueThisMonth(orderRepository.sumTotalAmountCreatedAtBetween(monthStart, nextMonthStart))
                .totalOrders(orderRepository.countAll())
                .ordersToday(orderRepository.countCreatedAtBetween(todayStart, tomorrowStart))
                .ordersByStatus(ordersByStatus)
                .ordersThisMonthByStatus(ordersThisMonthByStatus)
                .revenueChart(revenueChart)
                .build();
    }

}
