package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.port.in.usecase.GetProductDetailQueryUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProductDetailService implements GetProductDetailQueryUseCase {
    private final ProductReadRepository productReadRepository;
    private final ProductReviewStatsEnricher productReviewStatsEnricher;
    private final ProductTranslationService productTranslationService;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse execute(GetProductDetailQuery query) {
        ProductResponse product = findProductDetail(query.getSlug()).orElseThrow(
                () -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setReviews(null);
        productReviewStatsEnricher.enrich(product);

        return productTranslationService.localizeProduct(product, query.getLang());
    }

    private Optional<ProductResponse> findProductDetail(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            return Optional.empty();
        }

        try {
            return productReadRepository.findProductDetailById(UUID.fromString(idOrSlug));
        } catch (IllegalArgumentException ignored) {
            return productReadRepository.findProductDetailBySlug(idOrSlug);
        }
    }
}
