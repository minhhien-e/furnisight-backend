package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.MarketingDispatchLog;

public interface MarketingDispatchLogRepository {
    MarketingDispatchLog save(MarketingDispatchLog log);
}
