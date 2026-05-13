package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.furnisight.catalog.infrastructure.database.repository.jpa.product.ProductJpaRepository;

import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;
import com.furnisight.catalog.domain.repository.product.ProductQueryRepository;
import com.furnisight.catalog.domain.entities.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final ProductJpaRepository jpaProductRepository;

    @Override
    public Optional<ProductDetailResponseDto> findProductDetailById(UUID productId) {
        return jpaProductRepository.findById(productId).map(this::toResponseDto);
    }

    private ProductDetailResponseDto toResponseDto(Product product) {
        return ProductDetailResponseDto.builder()
                .id(product.getId())
                .shopId(product.getShopId())
                .categoryId(product.getCategoryId())           // Lay thang tu Product entity
                .categoryName(null)                            // TODO: can JOIN voi bang categories de lay ten
                .name(product.getName().getValue())
                .description(product.getDescription().getValue())
                .status(product.getProductStatus() != null ? product.getProductStatus().name() : null)
                .weight(product.getDimensions().getWeight())
                .length(product.getDimensions().getLength())
                .height(product.getDimensions().getHeight())
                .width(product.getDimensions().getWidth())
                .attributes(product.getAttributes() != null ? 
                    product.getAttributes().entrySet().stream()
                        .collect(Collectors.toMap(
                            java.util.Map.Entry::getKey, 
                            e -> String.valueOf(e.getValue()))) : null)
                .variants(product.getVariants() != null ? 
                    product.getVariants().stream()
                        .map(v -> ProductDetailResponseDto.VariantDto.builder()
                            .sku(v.getSku().getValue())
                            .price(v.getPrice().getValue().doubleValue())
                            .stockQuantity(v.getStockQuantity().getValue())
                            .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }
}

