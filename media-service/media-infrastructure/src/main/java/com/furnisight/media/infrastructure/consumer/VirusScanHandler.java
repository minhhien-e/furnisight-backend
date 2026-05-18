package com.furnisight.media.infrastructure.consumer;

import com.furnisight.media.core.model.event.MediaVirusScanCompletedEvent;
import com.furnisight.media.core.model.event.MediaVirusScanStartedEvent;
import com.furnisight.media.infrastructure.processing.VirusScanProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class VirusScanHandler {

    private final VirusScanProcessor virusScanProcessor;

    @EventListener
    public void handleValidated(MediaVirusScanStartedEvent event) {
        virusScanProcessor.process(event.mediaId());
    }

    @EventListener
    public void handleCompleted(MediaVirusScanCompletedEvent event) {
        virusScanProcessor.processSuccessful(event);
    }
}
