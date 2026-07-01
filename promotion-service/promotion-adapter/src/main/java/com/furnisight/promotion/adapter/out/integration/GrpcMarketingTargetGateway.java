package com.furnisight.promotion.adapter.out.integration;

import com.furnisight.admin.user.AdminUserServiceGrpc;
import com.furnisight.admin.user.BatchGetMarketingUsersRequest;
import com.furnisight.admin.user.FilterMarketingUserIdsRequest;
import com.furnisight.admin.user.ListMarketingUserIdsRequest;
import com.furnisight.admin.user.MarketingUserSegment;
import com.furnisight.internal.cart.InternalCartServiceGrpc;
import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import com.furnisight.promotion.domain.valueobjects.EmailAddress;

@Component
public class GrpcMarketingTargetGateway implements MarketingTargetGateway {

    @GrpcClient("user-service")
    private Channel userChannel;

    @GrpcClient("cart-service")
    private InternalCartServiceGrpc.InternalCartServiceBlockingStub cartStub;

    @Override
    public List<UUID> filterEligibleUserIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        var response = stub().filterMarketingUserIds(FilterMarketingUserIdsRequest.newBuilder()
                .addAllUserIds(userIds.stream().map(UUID::toString).toList())
                .build());
        if (!response.getAdministrativeUserIdsList().isEmpty()) {
            throw new IllegalArgumentException("Administrative users cannot receive marketing promotions: "
                    + String.join(",", response.getAdministrativeUserIdsList()));
        }
        return response.getEligibleUserIdsList().stream().map(UUID::fromString).toList();
    }

    @Override
    public List<UUID> getAllActiveUserIds() {
        return listUserIds(MarketingUserSegment.MARKETING_USER_SEGMENT_ALL);
    }

    @Override
    public List<UUID> getSegmentUserIds(String segmentKey) {
        return switch (segmentKey) {
            case "NEW_USERS" -> listUserIds(MarketingUserSegment.MARKETING_USER_SEGMENT_NEW_USERS);
            case "INACTIVE_30D" -> listUserIds(MarketingUserSegment.MARKETING_USER_SEGMENT_INACTIVE_30D);
            case "ABANDONED_CART" -> filterSegmentUserIds(getAbandonedCartUserIds());
            default -> throw new IllegalArgumentException("Invalid marketing segment: " + segmentKey);
        };
    }

    @Override
    public List<MarketingNotificationGateway.Recipient> getUsersByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return stub().batchGetMarketingUsers(BatchGetMarketingUsersRequest.newBuilder()
                        .addAllUserIds(userIds.stream().map(UUID::toString).toList())
                        .build())
                .getUsersList().stream()
                .filter(com.furnisight.admin.user.MarketingUserDto::getActive)
                .map(user -> new MarketingNotificationGateway.Recipient(
                        UUID.fromString(user.getUserId()),
                        new EmailAddress(user.getEmail()).getCleanValue(),
                        user.getName()))
                .toList();
    }

    private List<UUID> listUserIds(MarketingUserSegment segment) {
        return stub().listMarketingUserIds(ListMarketingUserIdsRequest.newBuilder().setSegment(segment).build())
                .getUserIdsList().stream().map(UUID::fromString).toList();
    }

    private List<UUID> filterSegmentUserIds(List<UUID> userIds) {
        if (userIds.isEmpty()) return List.of();
        var response = stub().filterMarketingUserIds(FilterMarketingUserIdsRequest.newBuilder()
                .addAllUserIds(userIds.stream().map(UUID::toString).toList()).build());
        return response.getEligibleUserIdsList().stream().map(UUID::fromString).toList();
    }

    private List<UUID> getAbandonedCartUserIds() {
        var response = cartStub.getAbandonedCartUserIds(Empty.getDefaultInstance());
        return response.getUserIdsList().stream().map(this::parseUuid).filter(java.util.Objects::nonNull).distinct().toList();
    }

    private AdminUserServiceGrpc.AdminUserServiceBlockingStub stub() {
        return AdminUserServiceGrpc.newBlockingStub(userChannel);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
