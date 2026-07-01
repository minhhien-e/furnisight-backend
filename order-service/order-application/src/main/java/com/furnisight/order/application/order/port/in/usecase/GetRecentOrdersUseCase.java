package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.query.GetRecentOrdersQuery;
import com.furnisight.order.domain.entities.order.Order;
import java.util.List;

public interface GetRecentOrdersUseCase {
    List<Order> getRecentOrders(GetRecentOrdersQuery query);
}
