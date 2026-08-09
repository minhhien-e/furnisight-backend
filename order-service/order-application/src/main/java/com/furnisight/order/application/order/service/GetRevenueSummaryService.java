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
        int months = 12;
        LocalDate firstDayOfMonth;
        
        if (query.getYear() > 0) {
            // For a specific year, we want 12 months starting from Dec backwards to Jan
            firstDayOfMonth = LocalDate.of(query.getYear(), 12, 1);
        } else {
            months = query.getMonths() > 0 ? Math.min(query.getMonths(), 24) : 12;
            firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime currentMonthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime currentNextMonthStart = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        double revenueThisMonth = orderRepository.sumTotalAmountCreatedAtBetween(currentMonthStart, currentNextMonthStart);
        long ordersThisMonth = orderRepository.countByStatusCreatedAtBetween(com.furnisight.order.domain.enums.OrderStatus.DELIVERED, currentMonthStart, currentNextMonthStart);

        List<MonthlyRevenueResult> monthlyList = new ArrayList<>();
        double prevRevenue = -1;
        for (int i = months - 1; i >= 0; i--) {
            LocalDate bucket = firstDayOfMonth.minusMonths(i);
            LocalDateTime start = bucket.atStartOfDay();
            LocalDateTime end = bucket.plusMonths(1).atStartOfDay();

            if (i == months - 1) { // Lấy doanh thu tháng trước đó để tính MoM cho tháng đầu tiên
                LocalDateTime prevStart = bucket.minusMonths(1).atStartOfDay();
                LocalDateTime prevEnd = bucket.atStartOfDay();
                prevRevenue = orderRepository.sumTotalAmountCreatedAtBetween(prevStart, prevEnd);
            }

            double revenue = orderRepository.sumTotalAmountCreatedAtBetween(start, end);
            long orderCount = orderRepository.countByStatusCreatedAtBetween(com.furnisight.order.domain.enums.OrderStatus.DELIVERED, start, end);

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
                .totalOrders(orderRepository.countByStatus(com.furnisight.order.domain.enums.OrderStatus.DELIVERED))
                .revenueThisMonth(revenueThisMonth)
                .ordersThisMonth(ordersThisMonth)
                .monthly(monthlyList)
                .build();
    }

}
