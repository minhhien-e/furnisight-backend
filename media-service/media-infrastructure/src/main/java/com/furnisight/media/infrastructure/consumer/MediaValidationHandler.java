package com.furnisight.media.infrastructure.consumer;

import com.furnisight.media.core.model.event.MediaProcessingStartedEvent;
import com.furnisight.media.core.model.event.MediaValidatedEvent;
import com.furnisight.media.infrastructure.processing.MediaValidationProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class MediaValidationHandler {

    private final MediaValidationProcessor mediaValidationProcessor;

    @EventListener
    public void handleStarted(MediaProcessingStartedEvent event) {
        mediaValidationProcessor.process(event.mediaId());
    }

    @EventListener
    public void handleSuccessful(MediaValidatedEvent event) {
        mediaValidationProcessor.processSuccessful(event);
    }
}
