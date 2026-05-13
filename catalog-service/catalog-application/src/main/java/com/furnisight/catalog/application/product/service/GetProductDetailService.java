package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;
import com.furnisight.catalog.application.product.port.in.usecase.GetProductDetailQueryUseCase;
import com.furnisight.catalog.domain.repository.product.ProductQueryRepository;
import com.furnisight.catalog.domain.exceptions.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductDetailService implements GetProductDetailQueryUseCase{
    private final ProductQueryRepository productQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponseDto execute(GetProductDetailQuery query){
        return productQueryRepository.findProductDetailById(query.getProductId()).orElseThrow(
            () -> new ProductNotFoundException(query.getProductId())
        );
    }
}
