package com.furnisight.review.infrastructure.database.repository.impl;

import com.furnisight.review.core.model.entity.ReviewProcessingJob;
import com.furnisight.review.core.repository.ReviewJobWritePort;
import com.furnisight.review.infrastructure.database.repository.jpa.ReviewJobJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewJobRepositoryImpl implements ReviewJobWritePort {

    private final ReviewJobJpaRepository jpaRepository;

    @Override
    public void save(ReviewProcessingJob job) {
        jpaRepository.save(job);
    }
}
