package com.furnisight.review.infrastructure.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.infrastructure.database.repository.jpa.ReviewJpaRepository;
import com.furnisight.review.infrastructure.elasticsearch.document.ReviewDocument;
import com.furnisight.review.infrastructure.elasticsearch.repository.ReviewEsRepository;
import com.furnisight.review.infrastructure.elasticsearch.mapper.ReviewEsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka Consumer lắng nghe các event thay đổi của Review từ topic "review-event"
 * và đồng bộ dữ liệu sang Elasticsearch index "reviews" một cách bất đồng bộ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewSyncConsumer {

    private final ReviewJpaRepository jpaRepository;
    private final ReviewEsRepository esRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "review-event", groupId = "furnisight_review_es_sync")
    public void consume(String rawMessage) {
        log.debug("Received Kafka event for ES review synchronization: {}", rawMessage);
        try {
            JsonNode rootNode = objectMapper.readTree(rawMessage);
            
            if (!rootNode.has("reviewId") || !rootNode.has("eventType")) {
                log.warn("Kafka event does not contain 'reviewId' or 'eventType'. Message skipped.");
                return;
            }
            
            String reviewIdStr = rootNode.get("reviewId").asText();
            String eventType = rootNode.get("eventType").asText();
            UUID reviewId = UUID.fromString(reviewIdStr);

            if ("REVIEW_DELETED".equalsIgnoreCase(eventType)) {
                log.debug("Deleting review {} from Elasticsearch due to REVIEW_DELETED event", reviewId);
                esRepository.deleteById(reviewIdStr);
                return;
            }

            // Sync: Fetch full data từ PostgreSQL và upsert vào ES
            jpaRepository.findById(reviewId).ifPresentOrElse(
                review -> {
                    log.debug("Upserting review {} to Elasticsearch. Status: {}", reviewId, review.getStatus());
                    ReviewDocument doc = ReviewEsMapper.toDocument(review);
                    esRepository.save(doc);
                },
                () -> {
                    // Nếu không tìm thấy trong Postgres DB, có thể đã bị xóa hoàn toàn
                    log.debug("Review {} not found in DB. Removing from Elasticsearch.", reviewId);
                    esRepository.deleteById(reviewIdStr);
                }
            );

        } catch (Exception e) {
            log.error("Failed to sync review to Elasticsearch. Raw message: {}", rawMessage, e);
        }
    }
}
