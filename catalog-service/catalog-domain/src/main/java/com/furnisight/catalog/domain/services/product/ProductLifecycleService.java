package com.furnisight.catalog.domain.services.product;

import com.furnisight.catalog.domain.entities.product.Product;
import com.furnisight.catalog.domain.exceptions.DuplicatedProductNameException;
import com.furnisight.catalog.domain.repository.product.ProductRepository;
import com.furnisight.catalog.domain.valueobjects.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductLifecycleService {

    private final ProductRepository productRepository;

    @lombok.Builder
    @lombok.Getter
    public static class VariantRequest {
        private final String sku;
        private final Double price;
        private final Integer stockQuantity;
    }

    public Product createProduct(UUID shopId, UUID categoryId, ProductName name, ProductDescription description,
                                 ProductDimensions dimensions, Map<String, Object> attributes,
                                 List<VariantRequest> variantRequests) {
        
        if (productRepository.existsByShopIdAndNameValue(shopId, name.getValue())) {
            throw new DuplicatedProductNameException("Product name is already exist");
        }

        List<Product.VariantData> variantDataList = new ArrayList<>();
        if (variantRequests != null) {
            for (VariantRequest req : variantRequests) {
                variantDataList.add(Product.VariantData.builder()
                        .sku(new SKU(req.getSku()))
                        .price(new Price(BigDecimal.valueOf(req.getPrice())))
                        .stockQuantity(new StockQuantity(req.getStockQuantity()))
                        .build());
            }
        }

        return Product.create(shopId, categoryId, name, description, dimensions, attributes, variantDataList);
    }
}
