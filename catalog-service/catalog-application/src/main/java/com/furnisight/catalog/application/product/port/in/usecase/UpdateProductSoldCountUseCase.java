package com.furnisight.catalog.application.product.port.in.usecase;

import java.util.Map;
import java.util.UUID;

public interface UpdateProductSoldCountUseCase {
    void execute(Map<UUID, Integer> productQuantities);
}
