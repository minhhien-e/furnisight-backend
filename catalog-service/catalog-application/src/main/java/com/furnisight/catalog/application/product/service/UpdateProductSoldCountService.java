package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductSoldCountUseCase;
import com.furnisight.catalog.application.product.port.out.ProductRepository;
import com.furnisight.catalog.domain.entities.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProductSoldCountService implements UpdateProductSoldCountUseCase {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void execute(Map<UUID, Integer> productQuantities) {
        if (productQuantities == null || productQuantities.isEmpty()) return;

        productQuantities.forEach((productId, quantity) -> {
            if (productId != null && quantity != null && quantity > 0) {
                productRepository.findById(productId).ifPresent(product -> {
                    product.incrementSoldCount(quantity);
                    productRepository.save(product);
                    log.info("Incremented sold count for product {} by {}", productId, quantity);
                });
            }
        });
    }
}
