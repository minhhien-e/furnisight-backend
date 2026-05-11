package com.furnisight.user.infrastructure.event.publisher.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.user.domain.entities.OutboxMessage;
import com.furnisight.user.domain.events.identity.*;
import com.furnisight.user.domain.events.profile.*;
import com.furnisight.user.domain.repository.OutboxMessageRepository;
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
    public void handle(AccountVerificationRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "account-verification-requested",
            payload
        ));
    }



    @SneakyThrows
    @EventListener
    public void handle(AccountResetPasswordRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "account-reset-password-requested",
            payload
        ));
    }

    /**
     * Handles OTP delivery for EMAIL_CHANGE flow (step 2).
     */
    @SneakyThrows
    @EventListener
    public void handle(EmailChangeOtpRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "email-change-otp-requested",
            payload
        ));
    }

    /**
     * Handles OTP delivery for PHONE_CHANGE flow (step 2).
     */
    @SneakyThrows
    @EventListener
    public void handle(PhoneChangeOtpRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "phone-change-otp-requested",
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(EmailLinkOtpRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "email-link-otp-requested",
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(PhoneLinkOtpRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "phone-link-otp-requested",
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(VerifyCurrentEmailOtpRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "verify-current-email-otp-requested",
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(VerifyCurrentPhoneOtpRequestedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.accountId().toString(),
            "verify-current-phone-otp-requested",
            payload
        ));
    }
}
