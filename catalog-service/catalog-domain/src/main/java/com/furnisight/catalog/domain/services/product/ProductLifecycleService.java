package com.furnisight.catalog.domain.services.product;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

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

@Service
@RequiredArgsConstructor
public class ProductLifecycleService {

    private final ProductRepository productRepository;

    public Product createProduct(
            UUID categoryId,
            ProductName name,
            ProductSlug slug,
            ProductDescription description,
            UUID modelMediaId,
            String modelUrl,
            Boolean supports3d,
            List<String> features,
            List<ProductImage> gallery,
            List<ProductVariant> variants) {

        if (productRepository.existsByNameValue(name.getValue())) {
            throw new AlreadyExistsException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }

        Product product = Product.create(
                categoryId,
                name,
                slug,
                description,
                modelMediaId,
                modelUrl,
                supports3d,
                features,
                gallery,
                variants);

        return product;
    }

    public void updateProfile(
            Product product,
            ProductName name,
            ProductSlug slug,
            ProductDescription description,
            UUID modelMediaId,
            String modelUrl,
            Boolean supports3d,
            List<String> features) {

        if (name != null && !name.equals(product.getName())) {
            if (productRepository.existsByNameValue(name.getValue())) {
                throw new AlreadyExistsException(ErrorCode.DUPLICATE_PRODUCT_NAME);
            }
        }

        product.updateProfile(name, slug, description, modelMediaId, modelUrl, supports3d, features);
    }

    /**
     * Khi sản phẩm đổi danh mục, tự động tính lại roomTypeHint.
     */
    public void changeCategory(Product product, UUID categoryId) {
        product.changeCategory(categoryId);
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
