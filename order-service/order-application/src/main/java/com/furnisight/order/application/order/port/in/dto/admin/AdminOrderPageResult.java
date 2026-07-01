package com.furnisight.order.application.order.port.in.dto.admin;

import com.furnisight.order.domain.entities.order.Order;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderPageResult {
    private List<Order> orders;
    private int currentPage;
    private long totalElements;
    private int totalPages;
}
