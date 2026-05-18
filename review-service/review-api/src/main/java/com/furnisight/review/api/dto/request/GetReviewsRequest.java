package com.furnisight.review.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GetReviewsRequest(
    @NotNull UUID productId,
    Integer page,
    Integer size
) {}
/**
 *  jdbc
 *  querydsl
 *  jooq
 */
