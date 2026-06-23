package com.furnisight.promotion.application.service;

import com.furnisight.promotion.application.dto.PublishVoucherCommand;
import com.furnisight.promotion.application.port.MarketingDispatchLogRepository;
import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import com.furnisight.promotion.application.port.PromotionRepository;
import com.furnisight.promotion.application.port.UserVoucherRepository;
import com.furnisight.promotion.domain.entities.Promotion;
import com.furnisight.promotion.domain.enums.MarketingChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketingDispatchServiceTest {
    private final MarketingTargetGateway targetGateway = mock(MarketingTargetGateway.class);
    private final MarketingNotificationGateway notificationGateway = mock(MarketingNotificationGateway.class);
    private final PromotionRepository promotionRepository = mock(PromotionRepository.class);
    private final UserVoucherRepository userVoucherRepository = mock(UserVoucherRepository.class);
    private final MarketingDispatchLogRepository dispatchLogRepository = mock(MarketingDispatchLogRepository.class);
    private MarketingService service;
    private UUID voucherId;

    @BeforeEach
    void setUp() {
        service = new MarketingService(null, null, null, null, dispatchLogRepository,
                promotionRepository, userVoucherRepository, notificationGateway, targetGateway, null);
        voucherId = UUID.randomUUID();
        when(promotionRepository.findById(voucherId)).thenReturn(Optional.of(Promotion.builder()
                .id(voucherId).code("SAVE10").name("Save ten").build()));
        when(notificationGateway.send(any(), any(), any(), any(), any()))
                .thenReturn(new MarketingNotificationGateway.DispatchResult(1, 1, 0, List.of()));
    }

    @Test
    void notificationOnlyDoesNotResolveEmailBatch() {
        UUID userId = UUID.randomUUID();
        when(targetGateway.filterEligibleUserIds(List.of(userId))).thenReturn(List.of(userId));

        service.publishVoucher(voucherId, command("MANUAL", List.of(userId.toString()), null,
                List.of("NOTIFICATION")));

        verify(targetGateway, never()).getUsersByIds(any());
        verify(notificationGateway).send(any(), any(), any(), eq(List.of(MarketingChannel.NOTIFICATION)), any());
    }

    @Test
    void manualEmailResolvesBatchFromSelectedUserIds() {
        UUID userId = UUID.randomUUID();
        when(targetGateway.filterEligibleUserIds(List.of(userId))).thenReturn(List.of(userId));
        when(targetGateway.getUsersByIds(List.of(userId))).thenReturn(List.of(
                new MarketingNotificationGateway.Recipient(userId, "user@example.com", "User")));

        service.publishVoucher(voucherId, command("MANUAL", List.of(userId.toString()), null,
                List.of("EMAIL")));

        verify(targetGateway).getUsersByIds(List.of(userId));
        verify(notificationGateway).send(any(), any(), any(), eq(List.of(MarketingChannel.EMAIL)), any());
    }

    @Test
    void segmentEmailResolvesIdsBeforeEmailBatch() {
        UUID userId = UUID.randomUUID();
        when(targetGateway.getSegmentUserIds("NEW_USERS")).thenReturn(List.of(userId));
        when(targetGateway.getUsersByIds(List.of(userId))).thenReturn(List.of(
                new MarketingNotificationGateway.Recipient(userId, "new@example.com", "New user")));

        service.publishVoucher(voucherId, command("SEGMENT", List.of(), "NEW_USERS", List.of("EMAIL")));

        verify(targetGateway).getSegmentUserIds("NEW_USERS");
        verify(targetGateway).getUsersByIds(List.of(userId));
    }

    @Test
    void rejectsEmptyChannelsAndVipSegment() {
        assertThatThrownBy(() -> service.publishVoucher(voucherId,
                command("MANUAL", List.of(UUID.randomUUID().toString()), null, List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("channel");

        assertThatThrownBy(() -> service.publishVoucher(voucherId,
                command("SEGMENT", List.of(), "VIP", List.of("EMAIL"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("segment");
    }

    private PublishVoucherCommand command(String targetType, List<String> userIds,
                                          String segmentKey, List<String> channels) {
        PublishVoucherCommand command = new PublishVoucherCommand();
        command.setTargetType(targetType);
        command.setTargetUserIds(userIds);
        command.setSegmentKey(segmentKey);
        command.setChannels(channels);
        return command;
    }
}
