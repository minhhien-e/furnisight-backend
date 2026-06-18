package com.furnisight.catalog.application.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.domain.entities.OutboxMessage;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductImage;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductUpdateEventService {
    public static final String TOPIC = "product-updated";

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public void enqueue(Product product) {
        try {
            ProductUpdatedPayload payload = toPayload(product);
            outboxMessageRepository.save(new OutboxMessage(
                    "Product",
                    product.getId().toString(),
                    TOPIC,
                    objectMapper.writeValueAsString(payload)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize product-updated event", exception);
        }
    }

    private ProductUpdatedPayload toPayload(Product product) {
        return new ProductUpdatedPayload(
                product.getId().toString(),
                product.getName() == null ? null : product.getName().getValue(),
                product.getSlug() == null ? null : product.getSlug().getValue(),
                firstImage(product),
                product.getProductStatus() == null ? null : product.getProductStatus().name(),
                product.getVariants() == null ? List.of() : product.getVariants().stream()
                        .map(this::toVariantPayload)
                        .toList()
        );
    }

    private String firstImage(Product product) {
        if (product.getGallery() == null || product.getGallery().isEmpty()) {
            return null;
        }
        return product.getGallery().stream()
                .sorted(Comparator.comparing(ProductImage::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getImageUrl)
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .findFirst()
                .orElse(null);
    }

    private ProductUpdatedVariantPayload toVariantPayload(ProductVariant variant) {
        return new ProductUpdatedVariantPayload(
                variant.getId() == null ? null : variant.getId().toString(),
                toDouble(variant.getPrice() == null ? null : variant.getPrice().getValue()),
                variant.getStockQuantity() == null ? null : variant.getStockQuantity().getValue(),
                variant.getDimensions() == null ? null : variant.getDimensions().getLength(),
                variant.getDimensions() == null ? null : variant.getDimensions().getWidth(),
                variant.getDimensions() == null ? null : variant.getDimensions().getHeight(),
                variant.getDimensions() == null ? null : variant.getDimensions().getWeight(),
                variant.getColor(),
                variant.getMaterial(),
                variant.getWarranty()
        );
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private record ProductUpdatedPayload(
            String productId,
            String name,
            String slug,
            String imageUrl,
            String status,
            List<ProductUpdatedVariantPayload> variants
    ) {
    }

    private record ProductUpdatedVariantPayload(
            String id,
            Double price,
            Integer stockQuantity,
            Double length,
            Double width,
            Double height,
            Double weight,
            String color,
            String material,
            String warranty
    ) {
    }
}
