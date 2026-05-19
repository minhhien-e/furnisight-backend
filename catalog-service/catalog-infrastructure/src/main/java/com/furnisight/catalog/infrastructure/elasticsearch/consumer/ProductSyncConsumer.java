package com.furnisight.catalog.infrastructure.elasticsearch.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.application.product.dto.projection.ProductEsProjection;
import com.furnisight.catalog.infrastructure.elasticsearch.document.ProductDocument;
import com.furnisight.catalog.infrastructure.elasticsearch.repository.ProductEsRepository;
import com.furnisight.catalog.infrastructure.elasticsearch.mapper.ProductEsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka Consumer lắng nghe các event thay đổi của Product từ topic "product-event"
 * và đồng bộ dữ liệu sang Elasticsearch index "products".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSyncConsumer {

    private final ProductReadRepository productReadRepository;
    private final ProductEsRepository productEsRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "product-event", groupId = "furnisight_catalog_es_sync")
    public void consume(String rawMessage) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawMessage);
            
            // Lấy productId từ payload event
            if (!rootNode.has("productId")) {
                log.warn("Kafka event does not contain 'productId'. Message skipped.");
                return;
            }
            
            String productIdStr = rootNode.get("productId").asText();
            UUID productId = UUID.fromString(productIdStr);

            // Kiểm tra xem event có phải là xóa hoặc chuyển sang trạng thái DELETED không
            boolean isDeleteEvent = false;
            if (rootNode.has("newStatus")) {
                String newStatus = rootNode.get("newStatus").asText();
                if ("DELETED".equalsIgnoreCase(newStatus)) {
                    isDeleteEvent = true;
                }
            }

            if (isDeleteEvent) {
                log.debug("Deleting product {} from Elasticsearch due to status DELETED", productId);
                productEsRepository.deleteById(productIdStr);
                return;
            }

            // Đồng bộ dữ liệu: Fetch full data từ PostgreSQL và upsert vào ES
            productReadRepository.findProductDocumentById(productId).ifPresentOrElse(
                projection -> {
                    if ("DELETED".equalsIgnoreCase(projection.getStatus())) {
                        log.debug("Deleting product {} from Elasticsearch because state is DELETED", productId);
                        productEsRepository.deleteById(productIdStr);
                    } else {
                        log.debug("Upserting product {} to Elasticsearch. Status: {}", productId, projection.getStatus());
                        ProductDocument doc = ProductEsMapper.toDocument(projection);
                        productEsRepository.save(doc);
                    }
                },
                () -> {
                    // Nếu không tìm thấy trong Postgres DB, có thể đã bị xóa hoàn toàn khỏi DB
                    log.debug("Product {} not found in DB. Removing from Elasticsearch.", productId);
                    productEsRepository.deleteById(productIdStr);
                }
            );

        } catch (Exception e) {
            log.error("Failed to sync product to Elasticsearch. Raw message: {}", rawMessage, e);
        }
    }
}
