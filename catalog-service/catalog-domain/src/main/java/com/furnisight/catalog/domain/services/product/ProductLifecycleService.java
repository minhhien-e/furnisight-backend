package com.furnisight.catalog.domain.services.product;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductImage;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.entities.ProductVariantImage;
import com.furnisight.catalog.domain.exceptions.AlreadyExistsException;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.valueobjects.product.ProductDescription;
import com.furnisight.catalog.domain.valueobjects.product.ProductName;
import com.furnisight.catalog.domain.valueobjects.product.ProductSlug;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
import com.furnisight.catalog.domain.valueobjects.product.ProductDimensions;
import com.furnisight.catalog.domain.enums.ProductStatus;
import java.util.ArrayList;
import java.util.Locale;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductLifecycleService {

    private final ProductRepository productRepository;

    public Product createProduct(
            UUID categoryId,
            String nameValue,
            String slugValue,
            String sku,
            String descriptionValue,
            List<String> features,
            List<String> imageUrls,
            List<ProductVariant> variants) {

        ProductName name = new ProductName(nameValue);
        ProductSlug slug = new ProductSlug(slugValue);
        ProductDescription description = new ProductDescription(descriptionValue);

        if (productRepository.existsByNameValue(name.getValue())) {
            throw new AlreadyExistsException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }

        List<ProductImage> gallery = new ArrayList<>();
        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                gallery.add(ProductImage.builder()
                        .id(UUID.randomUUID())
                        .imageUrl(imageUrl.trim())
                        .position(gallery.size())
                        .build());
            }
        }

        Product product = Product.create(
                categoryId,
                name,
                slug,
                sku,
                description,
                features,
                gallery,
                variants);

        return product;
    }

    public void updateProfile(
            Product product,
            String nameValue,
            String slugValue,
            String sku,
            String descriptionValue,
            List<String> features) {

        ProductName name = nameValue != null ? new ProductName(nameValue) : null;
        ProductSlug slug = slugValue != null ? new ProductSlug(slugValue) : null;
        ProductDescription description = descriptionValue != null ? new ProductDescription(descriptionValue) : null;

        if (name != null && !name.equals(product.getName())) {
            if (productRepository.existsByNameValue(name.getValue())) {
                throw new AlreadyExistsException(ErrorCode.DUPLICATE_PRODUCT_NAME);
            }
        }

        product.updateProfile(name, slug, sku, description, features);
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

    public void updateStatus(Product product, ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Product status is required");
        }
        switch (status) {
            case ACTIVE -> product.activate();
            case INACTIVE -> product.deactivate();
            default -> throw new IllegalArgumentException("Invalid product status: " + status);
        }
    }

    public void reserveInventory(Product product, UUID variantId, int quantity) {
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        variant.decreaseStock(quantity);
    }

    public void releaseInventory(Product product, UUID variantId, int quantity) {
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        variant.addStock(quantity);
    }

    public ProductVariant createVariant(
            String sku,
            Integer lowStockThreshold,
            Price price,
            StockQuantity stockQuantity,
            ProductDimensions dimensions,
            String material,
            String warranty,
            String color,
            UUID modelMediaId,
            String modelUrl,
            Boolean supports3d,
            List<String> imageUrls) {

        String formattedSku = sku == null ? "" : sku.trim().toUpperCase(Locale.ROOT);
        if (formattedSku.isBlank()) {
            throw new IllegalArgumentException("Variant SKU is required");
        }
        if (productRepository.findVariantIdBySku(formattedSku).isPresent()) {
            throw new IllegalArgumentException("Variant SKU already exists: " + formattedSku);
        }

        int threshold = lowStockThreshold == null || lowStockThreshold == 0 ? 5 : lowStockThreshold;
        if (threshold < 1 || threshold > 9999) {
            throw new IllegalArgumentException("Low stock threshold must be between 1 and 9999");
        }

        List<ProductVariantImage> images = new java.util.ArrayList<>();
        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                images.add(ProductVariantImage.builder()
                        .id(UUID.randomUUID())
                        .imageUrl(imageUrl.trim())
                        .position(images.size())
                        .build());
            }
        }

        ProductVariant variant = ProductVariant.builder()
                .id(UUID.randomUUID())
                .price(price)
                .stockQuantity(stockQuantity)
                .dimensions(dimensions)
                .material(material)
                .warranty(warranty)
                .color(color)
                .sku(formattedSku)
                .lowStockThreshold(threshold)
                .modelMediaId(modelMediaId)
                .modelUrl(modelUrl)
                .supports3d(supports3d != null ? supports3d : false)
                .images(images)
                .build();

        images.forEach(img -> img.setVariant(variant));

        return variant;
    }
}
