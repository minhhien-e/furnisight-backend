package com.furnisight.media.infrastructure.processing;

import com.furnisight.media.core.model.enums.AssetState;
import com.furnisight.media.core.model.enums.ProcessingJobType;
import com.furnisight.media.core.model.event.MediaProcessingFailedEvent;
import com.furnisight.media.core.model.event.MediaProcessingStartedEvent;
import com.furnisight.media.core.model.event.MediaUploadedEvent;
import com.furnisight.media.core.publisher.InternalEventPublisher;
import com.furnisight.media.core.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MediaUploadedProcessor {
    private final MediaAssetRepository mediaAssetRepository;
    private final InternalEventPublisher eventPublisher;

    public void process(MediaUploadedEvent event) {
        try {
            var mediaAsset = mediaAssetRepository.findById(event.mediaId());
            mediaAsset.setState(AssetState.PROCESSING);
            mediaAssetRepository.save(mediaAsset);

            eventPublisher.publish(new MediaProcessingStartedEvent(
                event.mediaId(),
                Instant.now()
            ));

        } catch (Exception e) {
            eventPublisher.publish(MediaProcessingFailedEvent.builder()
                .mediaId(event.mediaId())
                .jobType(ProcessingJobType.INIT)
                .errorMessage(e.getMessage())
                .occurredAt(Instant.now())
                .build());
        }
    }
}
