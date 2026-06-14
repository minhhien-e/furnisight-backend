package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.review.dto.ReviewProjection;
import com.furnisight.catalog.application.review.port.in.security.CurrentUserProvider;
import com.furnisight.catalog.application.review.port.in.usecase.CreateReviewUseCase;
import com.furnisight.catalog.application.review.port.in.usecase.DeleteReviewUseCase;
import com.furnisight.catalog.application.review.port.in.usecase.GetReviewsByProductUseCase;
import com.furnisight.catalog.application.review.port.in.usecase.GetTopRandomReviewsUseCase;
import com.furnisight.catalog.application.review.port.in.usecase.UpdateReviewUseCase;
import com.furnisight.catalog.presentation.web.rest.dto.request.review.CreateReviewRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.review.UpdateReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewsByProductUseCase getReviewsByProductUseCase;
    private final GetTopRandomReviewsUseCase getTopRandomReviewsUseCase;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateReviewRequest req) {
        createReviewUseCase.createReview(
            currentUserProvider.getCurrentUserId(),
            req.productId(),
            req.orderItemId(),
            req.title(),
            req.content(),
            req.rating(),
            req.userName(),
            req.userAvatarMediaId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteReviewUseCase.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewProjection>> getByProductId(
        @PathVariable UUID productId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getReviewsByProductUseCase.getReviewsByProduct(productId, page, size));
    }

    @GetMapping("/top-random")
    public ResponseEntity<List<ReviewProjection>> getTopRandomReviews(
        @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(getTopRandomReviewsUseCase.getTopRandomReviews(limit));
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> update(
       @RequestBody UpdateReviewRequest req
    ) {
        updateReviewUseCase.updateReview(
            req.reviewId(),
            req.title(),
            req.content(),
            req.rating()
        );
        return ResponseEntity.ok().build();
    }
}
