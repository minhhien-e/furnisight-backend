package com.furnisight.review.application.review.port.out;

import java.util.UUID;

public interface ReviewEventPublisherPort {
    void publishReviewChangedEvent(UUID productId);
}
