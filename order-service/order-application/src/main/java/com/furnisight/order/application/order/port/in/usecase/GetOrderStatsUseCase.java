package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.query.GetOrderStatsQuery;
import com.furnisight.order.application.order.port.in.dto.admin.OrderStatsResult;

public interface GetOrderStatsUseCase {
    OrderStatsResult getOrderStats(GetOrderStatsQuery query);
}
