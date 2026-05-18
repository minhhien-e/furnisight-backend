package com.furnisight.review.api.controller;

import com.furnisight.review.api.dto.request.GetReviewsRequest;
import com.furnisight.review.api.dto.request.ToggleVoteRequest;
import com.furnisight.review.api.dto.request.UpdateReviewRequest;
import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.service.Review.ReviewService;
import com.furnisight.review.api.dto.request.CreateReviewRequest;
import com.furnisight.review.core.service.ReviewVote.ReviewVoteService;
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
    private final ReviewVoteService reviewVoteService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateReviewRequest req) {
        reviewService.createReview(
            req.userId(),
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

    @GetMapping("/search")
    public ResponseEntity<List<ReviewResponse>> getByReviewId(
        @Valid @ModelAttribute GetReviewsRequest req
    ) {
        System.out.println(">>> Request ProductId: " + req.productId());

        return ResponseEntity.ok(reviewService.getReviewsByProduct(
            req.productId(),
            req.page(),
            req.size()
        ));
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> update(
        @Valid @RequestBody UpdateReviewRequest req
    ) {
        reviewService.updateReview(
            req.reviewId(),
            req.title(),
            req.content(),
            req.rating(),
            req.ipAddress()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/toggle-vote")
    public ResponseEntity<Void> toggleVote(@Valid @RequestBody ToggleVoteRequest req) {
        reviewVoteService.toggleVote(
            req.reviewId(),
            req.userId(),
            req.voteType()
        );
        return ResponseEntity.ok().build();
    }
}
