package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.UpdateProductVariantsCommand;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductVariantsUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.product.Product;
import com.furnisight.catalog.domain.valueobjects.product.*;
import com.furnisight.catalog.domain.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateProductVariantsService implements UpdateProductVariantsUseCase {
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void execute(UpdateProductVariantsCommand command) {
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        product.verifyOwnership(command.getShopId());

        List<Product.VariantData> variantDataList = command.getVariants().stream()
            .map(v -> Product.VariantData.builder()
                .sku(new SKU(v.getSku()))
                .price(new Price(BigDecimal.valueOf(v.getPrice())))
                .stockQuantity(new StockQuantity(v.getStockQuantity())).build()
        ).collect(Collectors.toList());

        product.updateVariants(variantDataList);
        productRepository.save(product);
    }
}
