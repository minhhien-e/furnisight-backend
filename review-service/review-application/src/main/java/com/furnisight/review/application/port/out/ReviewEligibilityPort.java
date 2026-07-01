package com.furnisight.review.application.port.out;

import java.util.UUID;

public interface ReviewEligibilityPort {

    boolean isEligible(UUID userId, UUID productId, UUID orderItemId);
}
