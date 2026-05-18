package com.furnisight.media.infrastructure.processing;

import com.furnisight.media.core.exception.MediaNotFoundException;
import com.furnisight.media.core.model.enums.AssetState;
import com.furnisight.media.core.model.enums.ProcessingJobStatus;
import com.furnisight.media.core.model.enums.ProcessingJobType;
import com.furnisight.media.core.model.event.MediaProcessingCompletedEvent;
import com.furnisight.media.core.model.event.MediaProcessingFailedEvent;
import com.furnisight.media.core.publisher.InternalEventPublisher;
import com.furnisight.media.core.repository.MediaAssetRepository;
import com.furnisight.media.core.repository.MediaProcessingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaProcessingProcessor {

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaProcessingJobRepository mediaProcessingJobRepository;
    private final InternalEventPublisher eventPublisher;

    public void processCompleted(MediaProcessingCompletedEvent event) {
        try {
            var mediaAsset = mediaAssetRepository.findById(event.mediaId());

            mediaAsset.setState(AssetState.ACTIVE);
            mediaAssetRepository.save(mediaAsset);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            eventPublisher.publish(MediaProcessingFailedEvent.builder()
                .mediaId(event.mediaId())
                .jobType(ProcessingJobType.INIT)
                .errorMessage(msg)
                .occurredAt(Instant.now())
                .build());
        }
    }

    public void processFailed(MediaProcessingFailedEvent event) {
        var mediaAsset = mediaAssetRepository.findById(event.mediaId());

        mediaAsset.setState(AssetState.FAILED);
        mediaAssetRepository.save(mediaAsset);

        var job = mediaProcessingJobRepository.findByStatusAndAssetId(ProcessingJobStatus.RUNNING, event.mediaId());
        job.setStatus(ProcessingJobStatus.FAILED);
        job.setErrorMessage(event.errorMessage());
        mediaProcessingJobRepository.save(job);
        log.error("No running job found for media {}", event.mediaId());
    }
}
