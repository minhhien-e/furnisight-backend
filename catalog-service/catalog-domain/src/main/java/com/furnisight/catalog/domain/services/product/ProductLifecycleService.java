package com.furnisight.catalog.domain.services.product;

import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductImage;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.exceptions.AlreadyExistsException;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.valueobjects.product.ProductDescription;
import com.furnisight.catalog.domain.valueobjects.product.ProductName;
import com.furnisight.catalog.domain.valueobjects.product.ProductSlug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductLifecycleService {

    private final ProductRepository productRepository;

    public Product createProduct(
            UUID categoryId,
            UUID collectionId,
            ProductName name,
            ProductSlug slug,
            ProductDescription description,
            Map<String, Object> attributes,
            Map<String, Object> metadata,
            Map<String, String> specs,
            List<ProductImage> gallery,
            List<ProductVariant> variants) {

        // Validate duplicate name
        if (productRepository.existsByNameValue(name.getValue())) {
            throw new AlreadyExistsException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }

        return Product.create(
                categoryId,
                collectionId,
                name,
                slug,
                description,
                attributes,
                metadata,
                specs,
                gallery,
                variants);
    }

    public void updateProfile(
            Product product,
            ProductName name,
            ProductSlug slug,
            ProductDescription description,
            Map<String, Object> attributes,
            Map<String, Object> metadata,
            Map<String, String> specs) {

        // If product name is changing, check duplication
        if (name != null && !name.equals(product.getName())) {
            if (productRepository.existsByNameValue(name.getValue())) {
                throw new AlreadyExistsException(ErrorCode.DUPLICATE_PRODUCT_NAME);
            }
        }

        product.updateProfile(
                name,
                slug,
                description,
                attributes,
                metadata,
                specs);
    }

    public void changeCategory(Product product, UUID categoryId) {
        product.changeCategory(categoryId);
    }

    public void assignToCollection(Product product, UUID collectionId) {
        product.assignToCollection(collectionId);
    }

    public void removeFromCollection(Product product) {
        product.removeFromCollection();
    }

    public void addImage(Product product, ProductImage image) {
        product.addImage(image);
    }

    public void moveImage(Product product, ProductImage image, int newPosition) {
        product.moveImage(image, newPosition);
    }

    public void removeImage(Product product, UUID imageId) {
        product.removeImage(imageId);
    }

    public void addVariant(Product product, ProductVariant variant) {
        product.addVariant(variant);
    }

    public void removeVariant(Product product, UUID variantId) {
        product.removeVariant(variantId);
    }

    public void activate(Product product) {
        product.activate();
    }

    public void deactivate(Product product) {
        product.deactivate();
    }
}
