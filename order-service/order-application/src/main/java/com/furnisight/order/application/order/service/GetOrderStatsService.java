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

        Map<String, Long> ordersByStatus = new HashMap<>();
        Map<String, Long> ordersThisMonthByStatus = new HashMap<>();

        Arrays.stream(OrderStatus.values()).forEach(status -> {
            ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
            ordersThisMonthByStatus.put(status.name(), orderRepository.countByStatusCreatedAtBetween(status, monthStart, nextMonthStart));
        });

        List<OrderStatsResult.ChartPointResult> revenueChart = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate bucket = today.minusMonths(i).withDayOfMonth(1);
            LocalDateTime start = bucket.atStartOfDay();
            LocalDateTime end = bucket.plusMonths(1).atStartOfDay();
            revenueChart.add(OrderStatsResult.ChartPointResult.builder()
                    .label(bucket.format(DATE_FORMAT))
                    .value(orderRepository.sumTotalAmountCreatedAtBetween(start, end))
                    .build());
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
