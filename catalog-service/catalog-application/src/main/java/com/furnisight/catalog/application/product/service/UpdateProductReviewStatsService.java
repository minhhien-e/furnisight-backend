package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.response.ProductReviewStats;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductReviewStatsUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.application.product.port.out.ProductReviewStatsPort;
import com.furnisight.catalog.domain.entities.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProductReviewStatsService implements UpdateProductReviewStatsUseCase {

    private final ProductRepository productRepository;
    private final ProductReviewStatsPort productReviewStatsPort;

    @Override
    @Transactional
    public void execute(UUID productId) {
        if (productId == null) return;

        productRepository.findById(productId).ifPresent(product -> {
            try {
                Map<UUID, ProductReviewStats> statsMap = productReviewStatsPort.getProductReviewStats(List.of(productId));
                ProductReviewStats stats = statsMap.get(productId);
                
                if (stats != null) {
                    product.updateReviewStats(stats.rating(), stats.ratingCount());
                    productRepository.save(product);
                    log.info("Updated review stats for product {}: rating={}, count={}", 
                            productId, stats.rating(), stats.ratingCount());
                }
            } catch (Exception e) {
                log.error("Failed to update review stats for product {}", productId, e);
            }
        });
    }
}
