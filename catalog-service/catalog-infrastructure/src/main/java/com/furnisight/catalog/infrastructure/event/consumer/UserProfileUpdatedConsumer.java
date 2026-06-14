package com.furnisight.catalog.infrastructure.event.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.review.port.in.usecase.UpdateReviewUserInfoUseCase;
import com.furnisight.catalog.infrastructure.event.dto.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileUpdatedConsumer {
    private static final String TOPIC = "user-profile-updated";

    private final UpdateReviewUserInfoUseCase updateReviewUserInfoUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        UserProfileUpdatedEvent event = objectMapper.readValue(payload, UserProfileUpdatedEvent.class);
        
        String fullName = String.join(" ",
                event.firstName() == null ? "" : event.firstName().trim(),
                event.lastName() == null ? "" : event.lastName().trim()
        ).trim();
        if (fullName.isBlank()) {
            fullName = null;
        }
        
        updateReviewUserInfoUseCase.updateUserInfo(event.accountId(), fullName, event.avatarMediaId());
    }
}
