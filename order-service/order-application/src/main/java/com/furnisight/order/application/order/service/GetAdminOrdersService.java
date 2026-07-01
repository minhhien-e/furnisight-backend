package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.usecase.GetAdminOrdersUseCase;
import com.furnisight.order.application.order.port.in.query.GetAdminOrdersQuery;
import com.furnisight.order.application.order.port.in.dto.admin.AdminOrderPageResult;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminOrdersService implements GetAdminOrdersUseCase {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminOrderPageResult getAdminOrders(GetAdminOrdersQuery query) {
        int page = Math.max(query.getPage() - 1, 0);
        int size = query.getSize() > 0 ? query.getSize() : DEFAULT_PAGE_SIZE;
        OrderStatus status = parseStatus(query.getStatus());

        List<Order> orders = status == null
                ? orderRepository.findAll(page, size)
                : orderRepository.findAllByStatus(status, page, size);
        long totalElements = status == null ? orderRepository.countAll() : orderRepository.countByStatus(status);
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        return AdminOrderPageResult.builder()
                .orders(orders)
                .currentPage(page + 1)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    private OrderStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        return OrderStatus.valueOf(rawStatus.trim().toUpperCase());
    }

}
