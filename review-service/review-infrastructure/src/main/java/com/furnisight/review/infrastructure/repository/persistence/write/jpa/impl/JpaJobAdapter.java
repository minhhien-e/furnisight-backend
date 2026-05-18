package com.furnisight.review.infrastructure.repository.persistence.write.jpa.impl;

import com.furnisight.review.core.model.entity.ReviewProcessingJob;
import com.furnisight.review.core.repository.ReviewJobWritePort;
import com.furnisight.review.infrastructure.repository.persistence.write.jpa.SpringDataJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaJobAdapter implements ReviewJobWritePort {

    private final SpringDataJobRepository jpaRepository;

    @Override
    public void save(ReviewProcessingJob job) {
        jpaRepository.save(job);
    }
}
