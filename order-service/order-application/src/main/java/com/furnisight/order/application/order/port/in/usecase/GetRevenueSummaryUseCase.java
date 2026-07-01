package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.query.GetRevenueSummaryQuery;
import com.furnisight.order.application.order.port.in.dto.admin.RevenueSummaryResult;

public interface GetRevenueSummaryUseCase {
    RevenueSummaryResult getRevenueSummary(GetRevenueSummaryQuery query);
}
