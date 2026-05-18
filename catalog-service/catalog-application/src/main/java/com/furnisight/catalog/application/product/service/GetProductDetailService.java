package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.port.in.usecase.GetProductDetailQueryUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.domain.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductDetailService implements GetProductDetailQueryUseCase{
    private final ProductReadRepository productReadRepository;
    private final com.furnisight.catalog.domain.repository.ProductRepository productRepository;

    @Override
    @Transactional
    public ProductDetailProjection execute(GetProductDetailQuery query){
        // Increment view count in domain entity
        productRepository.findById(query.getProductId()).ifPresent(product -> {
            product.incrementViewCount();
            productRepository.save(product);
        });

        return productReadRepository.findProductDetailById(query.getProductId()).orElseThrow(
            () -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND)
        );
    }
}
