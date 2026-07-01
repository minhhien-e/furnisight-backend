package com.furnisight.review.adapter.in.grpc;

import com.furnisight.review.BatchGetProductReviewStatsRequest;
import com.furnisight.review.BatchGetProductReviewStatsResponse;
import com.furnisight.review.CreateReviewRequest;
import com.furnisight.review.CreateReviewResponse;
import com.furnisight.review.DeleteReviewRequest;
import com.furnisight.review.GetTopRandomReviewsRequest;
import com.furnisight.review.GetTopRandomReviewsResponse;
import com.furnisight.review.ListProductReviewsRequest;
import com.furnisight.review.ListProductReviewsResponse;
import com.furnisight.review.ProductReviewStat;
import com.furnisight.review.ReviewActionResponse;
import com.furnisight.review.ReviewDto;
import com.furnisight.review.ReviewServiceGrpc;
import com.furnisight.review.UpdateReviewRequest;
import com.furnisight.review.application.review.dto.response.ReviewResponse;
import com.furnisight.review.application.review.port.in.usecase.CreateReviewUseCase;
import com.furnisight.review.application.review.port.in.usecase.DeleteReviewUseCase;
import com.furnisight.review.application.review.port.in.usecase.GetReviewsByProductUseCase;
import com.furnisight.review.application.review.port.in.usecase.GetTopRandomReviewsUseCase;
import com.furnisight.review.application.review.port.in.usecase.UpdateReviewUseCase;
import com.furnisight.review.application.review.port.out.repository.ReviewQueryRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ReviewGrpcService extends ReviewServiceGrpc.ReviewServiceImplBase {

    private final CreateReviewUseCase createReviewUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewsByProductUseCase getReviewsByProductUseCase;
    private final GetTopRandomReviewsUseCase getTopRandomReviewsUseCase;
    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    public void createReview(CreateReviewRequest request, StreamObserver<CreateReviewResponse> responseObserver) {
        try {
            createReviewUseCase.createReview(
                    UUID.fromString(request.getUserId()),
                    request.getProductId(),
                    request.getOrderItemId(),
                    request.getTitle(),
                    request.getContent(),
                    request.getRating(),
                    request.getUserName(),
                    request.getUserAvatarMediaId().isBlank() ? null : UUID.fromString(request.getUserAvatarMediaId())
            );
            responseObserver.onNext(CreateReviewResponse.newBuilder()
                    .setStatus("PENDING")
                    .setSentimentStatus("PENDING")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void updateReview(UpdateReviewRequest request, StreamObserver<ReviewActionResponse> responseObserver) {
        try {
            updateReviewUseCase.updateReview(
                    UUID.fromString(request.getReviewId()),
                    request.getTitle(),
                    request.getContent(),
                    request.getRating()
            );
            responseObserver.onNext(ReviewActionResponse.newBuilder().setSuccess(true).setMessage("updated").build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void deleteReview(DeleteReviewRequest request, StreamObserver<ReviewActionResponse> responseObserver) {
        try {
            deleteReviewUseCase.deleteReview(UUID.fromString(request.getReviewId()));
            responseObserver.onNext(ReviewActionResponse.newBuilder().setSuccess(true).setMessage("deleted").build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void listProductReviews(ListProductReviewsRequest request, StreamObserver<ListProductReviewsResponse> responseObserver) {
        try {
            var reviews = getReviewsByProductUseCase.getReviewsByProduct(
                    UUID.fromString(request.getProductId()),
                    request.getPage(),
                    request.getSize()
            );
            responseObserver.onNext(ListProductReviewsResponse.newBuilder()
                    .addAllReviews(reviews.stream().map(this::toGrpcReview).toList())
                    .setCurrentPage(request.getPage())
                    .setPageSize(request.getSize())
                    .setTotalElements(reviews.size())
                    .setTotalPages(1)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void getTopRandomReviews(GetTopRandomReviewsRequest request, StreamObserver<GetTopRandomReviewsResponse> responseObserver) {
        try {
            var reviews = getTopRandomReviewsUseCase.getTopRandomReviews(request.getLimit());
            responseObserver.onNext(GetTopRandomReviewsResponse.newBuilder()
                    .addAllReviews(reviews.stream().map(this::toGrpcReview).toList())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void batchGetProductReviewStats(BatchGetProductReviewStatsRequest request, StreamObserver<BatchGetProductReviewStatsResponse> responseObserver) {
        try {
            var stats = reviewQueryRepository.findProductStats(
                    request.getProductIdsList().stream().map(UUID::fromString).toList()
            );
            responseObserver.onNext(BatchGetProductReviewStatsResponse.newBuilder()
                    .addAllStats(stats.stream()
                            .map(stat -> ProductReviewStat.newBuilder()
                                    .setProductId(stat.productId().toString())
                                    .setAverageRating(stat.averageRating())
                                    .setRatingCount(stat.ratingCount())
                                    .setVisibleReviewCount(stat.visibleReviewCount())
                                    .build())
                            .toList())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    private ReviewDto toGrpcReview(ReviewResponse review) {
        ReviewDto.Builder builder = ReviewDto.newBuilder()
                .setId(review.id().toString())
                .setUserId(review.userId().toString())
                .setUserName(review.userName() == null ? "" : review.userName())
                .setUserAvatarUrl(review.userAvatarUrl() == null ? "" : review.userAvatarUrl())
                .setProductId(review.productId().toString())
                .setTitle(review.title() == null ? "" : review.title())
                .setContent(review.content() == null ? "" : review.content())
                .setRating(review.rating() == null ? 0 : review.rating())
                .setStatus(review.status() == null ? "" : review.status())
                .setCreatedAt(review.createdAt() == null ? "" : review.createdAt().toString());
        return builder.build();
    }
}
