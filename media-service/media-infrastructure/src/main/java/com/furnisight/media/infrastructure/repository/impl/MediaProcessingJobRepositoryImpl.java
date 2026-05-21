package com.furnisight.media.infrastructure.repository.impl;

import com.furnisight.media.core.exception.JobNotFoundException;
import com.furnisight.media.core.model.entity.MediaProcessingJob;
import com.furnisight.media.core.model.enums.ProcessingJobStatus;
import com.furnisight.media.core.repository.MediaProcessingJobRepository;
import com.furnisight.media.infrastructure.repository.jpa.MediaProcessingJobJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MediaProcessingJobRepositoryImpl implements MediaProcessingJobRepository {
    private final MediaProcessingJobJpaRepository jpaRepository;

    @Override
    public MediaProcessingJob findById(UUID id) {
        return jpaRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id.toString()));
    }

    @Override
    public MediaProcessingJob save(MediaProcessingJob job) {
        return jpaRepository.save(job);
    }

    @Override
    public MediaProcessingJob findByStatusAndAssetId(ProcessingJobStatus status, UUID mediaId) {
        return jpaRepository.findByStatusAndAssetId(status, mediaId).orElseThrow(() -> new JobNotFoundException(mediaId.toString()));
    }
}
