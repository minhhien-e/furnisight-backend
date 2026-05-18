package com.furnisight.review.api.controller;

import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewQueryController {

    @Qualifier("jdbcAdapter")
    private final ReviewQueryRepository reviewQueryRepository;

    @GetMapping("/product/{productId}")
    public List<ReviewResponse> getReviews(@PathVariable UUID productId) {
        return reviewQueryRepository.findByProductId(productId, 0, 10);
    }
}
