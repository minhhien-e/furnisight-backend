package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.port.in.usecase.GetProductDetailQueryUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductDetailService implements GetProductDetailQueryUseCase{
    private final ProductReadRepository productReadRepository;

    @Override
    @Transactional
    public ProductDetailProjection execute(GetProductDetailQuery query){
        return productReadRepository.findProductDetailBySlug(query.getSlug()).orElseThrow(
            () -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND)
        );
    }
}
