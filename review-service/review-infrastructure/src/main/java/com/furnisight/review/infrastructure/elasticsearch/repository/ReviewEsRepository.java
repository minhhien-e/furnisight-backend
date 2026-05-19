package com.furnisight.review.infrastructure.elasticsearch.repository;

import com.furnisight.review.infrastructure.elasticsearch.document.ReviewDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data Elasticsearch repository cho ReviewDocument.
 */
@Repository
public interface ReviewEsRepository extends ElasticsearchRepository<ReviewDocument, String> {
    List<ReviewDocument> findByProductIdAndStatusInOrderByCreatedAtDesc(String productId, List<String> statuses, Pageable pageable);
}
