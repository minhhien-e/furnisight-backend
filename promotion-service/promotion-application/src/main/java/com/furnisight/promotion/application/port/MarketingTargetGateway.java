package com.furnisight.promotion.application.port;

import java.util.List;
import java.util.UUID;

public interface MarketingTargetGateway {
    List<MarketingNotificationGateway.Recipient> getUsersByIds(List<UUID> userIds);
    List<MarketingNotificationGateway.Recipient> getAllActiveUsers();
    List<MarketingNotificationGateway.Recipient> getSegmentUsers(String segmentKey);
}
