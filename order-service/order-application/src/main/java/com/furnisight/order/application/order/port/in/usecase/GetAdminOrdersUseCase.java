package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.query.GetAdminOrdersQuery;
import com.furnisight.order.application.order.port.in.dto.admin.AdminOrderPageResult;

public interface GetAdminOrdersUseCase {
    AdminOrderPageResult getAdminOrders(GetAdminOrdersQuery query);
}
