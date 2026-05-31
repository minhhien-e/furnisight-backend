package com.furnisight.user.infrastructure.messaging.domain.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.user.domain.entities.OutboxMessage;
import com.furnisight.user.domain.events.identity.*;
import com.furnisight.user.domain.events.profile.UserProfileUpdatedEvent;
import com.furnisight.user.domain.repository.OutboxMessageRepository;
import com.furnisight.user.infrastructure.messaging.EventTopics;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountEventToOutboxListener {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE = "Account";

    @SneakyThrows
    @EventListener
    public void handle(AccountCreatedEvent event) {
        save(event.accountId().toString(), EventTopics.ACCOUNT_CREATED, event);
    }

    @SneakyThrows
    @EventListener
    public void handle(AccountDeletedEvent event) {
        save(event.accountId().toString(), EventTopics.ACCOUNT_DELETED, event);
    }

    @SneakyThrows
    @EventListener
    public void handle(AccountVerificationRequestedEvent event) {
        save(event.accountId().toString(), EventTopics.ACCOUNT_VERIFICATION_REQUESTED, event);
    }

    @SneakyThrows
    @EventListener
    public void handle(AccountResetPasswordRequestedEvent event) {
        save(event.accountId().toString(), EventTopics.ACCOUNT_RESET_PASSWORD_REQUESTED, event);
    }

    @SneakyThrows
    @EventListener
    public void handle(SocialAccountCreatedEvent event) {
        save(event.accountId().toString(), EventTopics.SOCIAL_ACCOUNT_CREATED, event);
    }

    @SneakyThrows
    @EventListener
    public void handle(UserProfileUpdatedEvent event) {
        save(event.getAccountId().toString(), EventTopics.USER_PROFILE_UPDATED, event);
    }

    private void save(String aggregateId, String topic, Object event) throws com.fasterxml.jackson.core.JsonProcessingException {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            aggregateId,
            topic,
            payload
        ));
    }
}
