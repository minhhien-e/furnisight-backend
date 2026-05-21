package com.furnisight.media.infrastructure.consumer;

import com.furnisight.media.core.model.entity.MediaAsset;
import com.furnisight.media.core.model.event.MediaUploadedEvent;
import com.furnisight.media.core.model.event.MediaVirusScanCompletedEvent;
import com.furnisight.media.core.repository.MediaAssetRepository;
import com.furnisight.media.infrastructure.processing.MediaUploadedProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class MediaUploadedHandler {
    private final MediaUploadedProcessor mediaUploadedProcessor;

    @EventListener
    public void handle(MediaUploadedEvent event) {
        mediaUploadedProcessor.process(event);
    }
}
