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

@Service
@RequiredArgsConstructor
public class GetProductDetailService implements GetProductDetailQueryUseCase {
    private final ProductReadRepository productReadRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductDetailProjection execute(GetProductDetailQuery query) {
        ProductDetailProjection product = productReadRepository.findProductDetailBySlug(query.getSlug()).orElseThrow(
                () -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        List<ReviewProjection> reviews = reviewQueryRepository.findByProductId(product.getId(), 0, 20);
        List<ProductDetailProjection.Review> detailReviews = reviews.stream()
                .filter(r -> "VISIBLE".equalsIgnoreCase(r.status()))
                .map(r -> {
                    String userId = r.userId() != null ? r.userId().toString() : "";
                    String displayUser = "Người dùng " + (userId.length() > 4 ? userId.substring(0, 4) : userId);
                    return ProductDetailProjection.Review.builder()
                            .id(r.id().toString())
                            .user(displayUser)
                            .avatar("https://i.pravatar.cc/150?u=" + userId)
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
}
