package com.furnisight.catalog.infrastructure.elasticsearch.repository;

import com.furnisight.catalog.infrastructure.elasticsearch.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Elasticsearch repository cho ProductDocument.
 * Cung cấp CRUD cơ bản, Spring tự động tạo implementation.
 */
@Repository
public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, String> {
}
