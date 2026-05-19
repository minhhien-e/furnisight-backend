package com.furnisight.review.infrastructure.database.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.core.repository.ReviewWritePort;
import com.furnisight.review.infrastructure.database.repository.jpa.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Triển khai ghi (Write Model) cho Review, thực hiện lưu vào PostgreSQL database
 * và gửi Event sang Kafka để đồng bộ bất đồng bộ dữ liệu tới Elasticsearch.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ReviewRepositoryImpl implements ReviewWritePort {

    private final ReviewJpaRepository jpaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "review-event";

    @Override
    @Transactional
    public void save(Review review) {
        // 1. Lưu vào PostgreSQL database
        jpaRepository.save(review);
        log.debug("Saved review {} in PostgreSQL database", review.getId());

        // 2. Gửi event sang Kafka để cập nhật ES bất đồng bộ sau
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "reviewId", review.getId().toString(),
                    "productId", review.getProductId().toString(),
                    "eventType", "REVIEW_SAVED"
            ));
            kafkaTemplate.send(TOPIC, review.getId().toString(), payload);
            log.debug("Published REVIEW_SAVED event for review {} to Kafka", review.getId());
        } catch (Exception e) {
            log.error("Failed to publish REVIEW_SAVED event for review {}. System continues operation.", review.getId(), e);
        }
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        // 1. Xóa từ PostgreSQL database
        jpaRepository.deleteById(id);
        log.debug("Deleted review {} from PostgreSQL database", id);

        // 2. Gửi event sang Kafka để xóa khỏi ES bất đồng bộ sau
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "reviewId", id.toString(),
                    "eventType", "REVIEW_DELETED"
            ));
            kafkaTemplate.send(TOPIC, id.toString(), payload);
            log.debug("Published REVIEW_DELETED event for review {} to Kafka", id);
        } catch (Exception e) {
            log.error("Failed to publish REVIEW_DELETED event for review {}. System continues operation.", id, e);
        }
    }
}
