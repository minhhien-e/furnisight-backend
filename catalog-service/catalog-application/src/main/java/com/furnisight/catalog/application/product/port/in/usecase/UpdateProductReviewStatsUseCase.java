package com.furnisight.catalog.application.product.port.in.usecase;

import java.util.UUID;

public interface UpdateProductReviewStatsUseCase {
    void execute(UUID productId);
}
