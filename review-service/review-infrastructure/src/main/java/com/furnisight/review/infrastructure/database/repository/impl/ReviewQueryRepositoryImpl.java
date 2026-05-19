package com.furnisight.review.infrastructure.database.repository.impl;

import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.core.model.enums.ReviewStatus;
import com.furnisight.review.core.repository.ReviewQueryRepository;
import com.furnisight.review.infrastructure.database.repository.jpa.ReviewJpaRepository;
import com.furnisight.review.infrastructure.elasticsearch.document.ReviewDocument;
import com.furnisight.review.infrastructure.elasticsearch.repository.ReviewEsRepository;
import com.furnisight.review.infrastructure.elasticsearch.mapper.ReviewEsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Triển khai đọc (Query Model) cho Review sử dụng Elasticsearch làm công cụ truy vấn chính.
 * Tích hợp cơ chế tự động phòng vệ lỗi (Fallback) chuyển tiếp về PostgreSQL qua JPA khi ES gặp sự cố.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final ReviewEsRepository esRepository;
    private final ReviewJpaRepository jpaRepository;

    @Override
    public List<ReviewResponse> findByProductId(UUID productId, Integer page, Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        try {
            log.debug("Querying reviews for product {} from Elasticsearch", productId);
            List<String> esStatuses = List.of("PENDING", "VISIBLE");
            List<ReviewDocument> docs = esRepository.findByProductIdAndStatusInOrderByCreatedAtDesc(
                    productId.toString(), esStatuses, pageable);

            return docs.stream()
                    .map(ReviewEsMapper::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Elasticsearch query failed for product {}, falling back to PostgreSQL database via JPA", productId, e);
            
            // Cơ chế phòng vệ: Fallback về PostgreSQL database thông qua JPA
            List<ReviewStatus> jpaStatuses = List.of(ReviewStatus.PENDING, ReviewStatus.VISIBLE);
            List<Review> entities = jpaRepository.findByProductIdAndStatusInOrderByCreatedAtDesc(
                    productId, jpaStatuses, pageable);

            return entities.stream()
                    .map(entity -> new ReviewResponse(
                            entity.getId(),
                            entity.getUserId(),
                            entity.getProductId(),
                            entity.getTitle().value(),
                            entity.getContent().text(),
                            entity.getRating().value(),
                            entity.getStatus().name(),
                            entity.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
        }
    }
}
