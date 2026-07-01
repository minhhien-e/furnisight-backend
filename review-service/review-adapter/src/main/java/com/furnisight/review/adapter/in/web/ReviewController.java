package com.furnisight.review.adapter.in.web;

import com.furnisight.review.application.review.dto.response.ReviewResponse;
import com.furnisight.review.application.review.port.in.security.CurrentUserProvider;
import com.furnisight.review.application.review.port.in.usecase.CreateReviewUseCase;
import com.furnisight.review.application.review.port.in.usecase.DeleteReviewUseCase;
import com.furnisight.review.application.review.port.in.usecase.GetCurrentUserReviewsByOrderItemsUseCase;
import com.furnisight.review.application.review.port.in.usecase.GetReviewsByProductUseCase;
import com.furnisight.review.application.review.port.in.usecase.GetTopRandomReviewsUseCase;
import com.furnisight.review.application.review.port.in.usecase.UpdateReviewUseCase;
import com.furnisight.review.adapter.in.web.CreateReviewRequest;
import com.furnisight.review.adapter.in.web.UpdateReviewRequest;
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
    private final GetCurrentUserReviewsByOrderItemsUseCase getCurrentUserReviewsByOrderItemsUseCase;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping({"", "/"})
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
    public ResponseEntity<Void> delete(@PathVariable(value = "id") UUID id) {
        deleteReviewUseCase.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getByProductId(
        @PathVariable(value = "productId") UUID productId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getReviewsByProductUseCase.getReviewsByProduct(productId, page, size));
    }

    @GetMapping("/top-random")
    public ResponseEntity<List<ReviewResponse>> getTopRandomReviews(
        @RequestParam(name = "limit", defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(getTopRandomReviewsUseCase.getTopRandomReviews(limit));
    }

    @GetMapping("/me/order-items")
    public ResponseEntity<List<ReviewResponse>> getCurrentUserReviewsByOrderItems(
        @RequestParam(name = "orderItemIds", required = false) List<UUID> orderItemIds
    ) {
        return ResponseEntity.ok(
            getCurrentUserReviewsByOrderItemsUseCase.getCurrentUserReviewsByOrderItemIds(
                currentUserProvider.getCurrentUserId(),
                orderItemIds
            )
        );
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

