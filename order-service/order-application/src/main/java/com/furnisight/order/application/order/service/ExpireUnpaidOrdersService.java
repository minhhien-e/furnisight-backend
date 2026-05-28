package com.furnisight.order.application.order.service;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpireUnpaidOrdersService {
    public static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(5);

    private static final List<OrderStatus> EXPIRABLE_STATUSES = List.of(
            OrderStatus.UNPAID,
            OrderStatus.PAYMENT_FAILED
    );

    private final OrderRepository orderRepository;

    @Scheduled(
            initialDelayString = "${orders.payment-expiration-initial-delay-ms:30000}",
            fixedDelayString = "${orders.payment-expiration-scan-delay-ms:60000}"
    )
    @Transactional
    public void expireOverdueOrders() {
        int expiredCount = expireUnpaidOrders(LocalDateTime.now());
        if (expiredCount > 0) {
            log.info("Expired {} unpaid orders after {} minutes", expiredCount, PAYMENT_TIMEOUT.toMinutes());
        }
    }

    public int expireUnpaidOrders(LocalDateTime now) {
        LocalDateTime cutoff = now.minus(PAYMENT_TIMEOUT);
        List<Order> overdueOrders = orderRepository.findAllByStatusesAndCreatedAtBefore(EXPIRABLE_STATUSES, cutoff);

        for (Order order : overdueOrders) {
            order.cancelOrder();
            orderRepository.save(order);
        }

        return overdueOrders.size();
    }

    public static boolean isPaymentWindowOpen(Order order, LocalDateTime now) {
        LocalDateTime createdAt = order.getCreatedAt();
        if (createdAt == null) {
            return true;
        }

        return createdAt.plus(PAYMENT_TIMEOUT).isAfter(now);
    }

    public static LocalDateTime paymentExpiresAt(Order order) {
        LocalDateTime createdAt = order.getCreatedAt();
        return createdAt != null ? createdAt.plus(PAYMENT_TIMEOUT) : null;
    }
}
