package com.furnisight.promotion.application.port;

import java.util.List;
import java.util.UUID;

public interface MarketingTargetGateway {
    List<UUID> filterEligibleUserIds(List<UUID> userIds);
    List<UUID> getAllActiveUserIds();
    List<UUID> getSegmentUserIds(String segmentKey);
    List<MarketingNotificationGateway.Recipient> getUsersByIds(List<UUID> userIds);
}
