package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.usecase.GetRevenueSummaryUseCase;
import com.furnisight.order.application.order.port.in.query.GetRevenueSummaryQuery;
import com.furnisight.order.application.order.port.in.dto.admin.RevenueSummaryResult;
import com.furnisight.order.application.order.port.in.dto.admin.MonthlyRevenueResult;
import com.furnisight.order.domain.repository.order.OrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRevenueSummaryService implements GetRevenueSummaryUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public RevenueSummaryResult getRevenueSummary(GetRevenueSummaryQuery query) {
        int months = query.getMonths() > 0 ? Math.min(query.getMonths(), 24) : 12;
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime nextMonthStart = firstDayOfMonth.plusMonths(1).atStartOfDay();
        double revenueThisMonth = orderRepository.sumTotalAmountCreatedAtBetween(monthStart, nextMonthStart);
        long ordersThisMonth = orderRepository.countCreatedAtBetween(monthStart, nextMonthStart);

        List<MonthlyRevenueResult> monthlyList = new ArrayList<>();
        double prevRevenue = -1;
        for (int i = months - 1; i >= 0; i--) {
            LocalDate bucket = firstDayOfMonth.minusMonths(i);
            LocalDateTime start = bucket.atStartOfDay();
            LocalDateTime end = bucket.plusMonths(1).atStartOfDay();

            double revenue = orderRepository.sumTotalAmountCreatedAtBetween(start, end);
            long orderCount = orderRepository.countCreatedAtBetween(start, end);

            double momChangePct = 0;
            if (prevRevenue > 0) {
                momChangePct = Math.round(((revenue - prevRevenue) / prevRevenue) * 10000D) / 100D;
            } else if (prevRevenue == 0 && revenue > 0) {
                momChangePct = 100D;
            }
            prevRevenue = revenue;

            monthlyList.add(MonthlyRevenueResult.builder()
                    .yearMonth(bucket.toString())
                    .revenue(revenue)
                    .orderCount(orderCount)
                    .momChangePct(momChangePct)
                    .build());
        }

        return RevenueSummaryResult.builder()
                .totalRevenue(orderRepository.sumTotalAmount())
                .totalOrders(orderRepository.countAll())
                .revenueThisMonth(revenueThisMonth)
                .ordersThisMonth(ordersThisMonth)
                .monthly(monthlyList)
                .build();
    }

}
