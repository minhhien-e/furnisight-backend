package com.furnisight.review.infrastructure.database.repository.jpa;

import com.furnisight.review.core.model.entity.ReviewProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewJobJpaRepository extends JpaRepository<ReviewProcessingJob, UUID> {
}
