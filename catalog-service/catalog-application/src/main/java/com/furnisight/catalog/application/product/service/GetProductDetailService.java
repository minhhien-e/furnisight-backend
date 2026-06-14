package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.port.in.usecase.GetProductDetailQueryUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.application.review.dto.ReviewProjection;
import com.furnisight.catalog.application.review.port.out.repository.ReviewQueryRepository;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProductDetailService implements GetProductDetailQueryUseCase {
    private final ProductReadRepository productReadRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductDetailProjection execute(GetProductDetailQuery query) {
        ProductDetailProjection product = findProductDetail(query.getSlug()).orElseThrow(
                () -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        List<ReviewProjection> reviews = reviewQueryRepository.findByProductId(product.getId(), 0, 20);
        List<ProductDetailProjection.Review> detailReviews = reviews.stream()
                .filter(r -> "VISIBLE".equalsIgnoreCase(r.status()))
                .map(r -> {
                    String displayUser = r.userName() != null && !r.userName().isBlank()
                            ? r.userName()
                            : "Khách hàng";
                    return ProductDetailProjection.Review.builder()
                            .id(r.id().toString())
                            .user(displayUser)
                            .avatar(r.userAvatarUrl())
                            .rating(r.rating())
                            .createdAt(r.createdAt().toString())
                            .comment(r.content())
                            .build();
                }).toList();

        product.setReviews(detailReviews);
        if (!detailReviews.isEmpty()) {
            product.setRatingCount(detailReviews.size());
            double avgRating = detailReviews.stream().mapToInt(ProductDetailProjection.Review::getRating).average()
                    .orElse(0.0);
            product.setRating(Math.round(avgRating * 10.0) / 10.0);
        } else {
            product.setRatingCount(0);
            product.setRating(0.0);
        }

        return product;
    }

    private Optional<ProductDetailProjection> findProductDetail(String idOrSlug) {
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
