package com.furnisight.review.api.dto.request;

import com.furnisight.review.core.model.enums.VoteType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ToggleVoteRequest(
    @NotNull(message = "Review ID is required") UUID reviewId,
    @NotNull(message = "User ID is required") UUID userId,
    @NotNull(message = "Vote type is required") VoteType voteType
) {}
