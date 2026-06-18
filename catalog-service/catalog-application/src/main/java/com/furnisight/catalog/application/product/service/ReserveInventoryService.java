package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.UpdateInventoryCommand;
import com.furnisight.catalog.application.product.port.in.usecase.ReserveInventoryUseCase;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import com.furnisight.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveInventoryService implements ReserveInventoryUseCase {

    private final ProductRepository productRepository;
    private final ProductUpdateEventService productUpdateEventService;

    @Override
    @Transactional
    public Void execute(UpdateInventoryCommand command) {
        log.info("Reserving inventory for order: {}", command.getOrderCode());

        for (UpdateInventoryCommand.StockItem item : command.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(item.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

            variant.decreaseStock(item.getQuantity());
            productRepository.save(product);
            productUpdateEventService.enqueue(product);
        }

        return null;
    }
}
