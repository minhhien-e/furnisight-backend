package com.furnisight.review.api.controller;

import com.furnisight.review.api.dto.request.UpdateReviewRequest;
import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.service.Review.ReviewService;
import com.furnisight.review.api.dto.request.CreateReviewRequest;
import com.furnisight.review.core.security.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateReviewRequest req) {
        reviewService.createReview(
            currentUserProvider.getCurrentUserId(),
            req.productId(),
            req.orderItemId(),
            req.title(),
            req.content(),
            req.rating()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getByProductId(
        @RequestParam UUID productId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, page, size));
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> update(
        @Valid @RequestBody UpdateReviewRequest req
    ) {
        reviewService.updateReview(
            req.reviewId(),
            req.title(),
            req.content(),
            req.rating()
        );
        return ResponseEntity.ok().build();
    }
}
