package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MarketingNotificationRequestedConsumerTest {
    @Test
    void forwardsSelectedChannelAndResolvedEmail() {
        SendNotificationUseCase useCase = mock(SendNotificationUseCase.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        var consumer = new MarketingNotificationRequestedConsumer(useCase, objectMapper);

        consumer.handle("""
                {"eventId":"c4dd233b-3dfa-472e-a86d-d95f02421fba",
                 "occurredOn":"2026-06-23T10:00:00",
                 "userId":"aa4455d2-a256-40f5-95a6-1d3147f27045",
                 "destination":"user@example.com","title":"Sale","body":"Voucher",
                 "actionUrl":"/account/vouchers","channel":"EMAIL"}
                """);

        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(useCase).execute(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(captor.getValue().getDestination()).isEqualTo("user@example.com");
    }
}
