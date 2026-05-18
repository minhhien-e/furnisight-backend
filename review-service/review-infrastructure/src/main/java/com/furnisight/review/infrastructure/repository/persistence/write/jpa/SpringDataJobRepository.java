package com.furnisight.review.infrastructure.repository.persistence.write.jpa;

import com.furnisight.review.core.model.entity.ReviewProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataJobRepository extends JpaRepository<ReviewProcessingJob, UUID> {
}
