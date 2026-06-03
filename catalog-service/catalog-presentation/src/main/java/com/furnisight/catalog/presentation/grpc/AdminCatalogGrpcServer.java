package com.furnisight.catalog.presentation.grpc;

import com.furnisight.admin.catalog.AdminActionResponse;
import com.furnisight.admin.catalog.AdminCatalogServiceGrpc;
import com.furnisight.admin.catalog.CategoryDto;
import com.furnisight.admin.catalog.CategoryListResponse;
import com.furnisight.admin.catalog.CategoryStatsResponse;
import com.furnisight.admin.catalog.CreateCategoryRequest;
import com.furnisight.admin.catalog.CreateProductRequest;
import com.furnisight.admin.catalog.DeleteCategoryRequest;
import com.furnisight.admin.catalog.DeleteProductRequest;
import com.furnisight.admin.catalog.GetAdminCategoriesRequest;
import com.furnisight.admin.catalog.GetAdminProductsRequest;
import com.furnisight.admin.catalog.GetCategoryDetailRequest;
import com.furnisight.admin.catalog.GetLowStockProductsRequest;
import com.furnisight.admin.catalog.GetProductDetailRequest;
import com.furnisight.admin.catalog.LowStockProductDto;
import com.furnisight.admin.catalog.LowStockProductListResponse;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.ProductPageResponse;
import com.furnisight.admin.catalog.ProductStatsResponse;
import com.furnisight.admin.catalog.ProductVariantDto;
import com.furnisight.admin.catalog.ProductVariantInput;
import com.furnisight.admin.catalog.StockInVariantRequest;
import com.furnisight.admin.catalog.UpdateCategoryRequest;
import com.furnisight.admin.catalog.UpdateProductRequest;
import com.furnisight.catalog.application.category.dto.command.CreateCategoryCommand;
import com.furnisight.catalog.application.category.dto.command.UpdateCategoryCommand;
import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import com.furnisight.catalog.application.category.port.in.usecase.CreateCategoryUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.UpdateCategoryUseCase;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import com.furnisight.catalog.application.product.dto.command.ChangeProductCategoryCommand;
import com.furnisight.catalog.application.product.dto.command.CreateProductCommand;
import com.furnisight.catalog.application.product.dto.command.UpdateInventoryCommand;
import com.furnisight.catalog.application.product.dto.command.UpdateProductInfoCommand;
import com.furnisight.catalog.application.product.dto.command.UpdateProductStatusCommand;
import com.furnisight.catalog.application.product.dto.projection.AdminProductProjection;
import com.furnisight.catalog.application.product.dto.projection.LowStockProductProjection;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.port.in.usecase.ChangeProductCategoryUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.CreateProductUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.ReleaseInventoryUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductInfoUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductStatusUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.domain.entities.ProductImage;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.enums.ProductStatus;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.ProductDimensions;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
import io.grpc.stub.StreamObserver;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AdminCatalogGrpcServer extends AdminCatalogServiceGrpc.AdminCatalogServiceImplBase {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductReadRepository productReadRepository;
    private final ProductRepository productRepository;
    private final CategoryReadRepository categoryReadRepository;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductInfoUseCase updateProductInfoUseCase;
    private final UpdateProductStatusUseCase updateProductStatusUseCase;
    private final ChangeProductCategoryUseCase changeProductCategoryUseCase;
    private final ReleaseInventoryUseCase releaseInventoryUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;

    @Override
    public void getAdminProducts(GetAdminProductsRequest request, StreamObserver<ProductPageResponse> responseObserver) {
        try {
            int page = Math.max(request.getPage() - 1, 0);
            int size = request.getSize() > 0 ? request.getSize() : DEFAULT_PAGE_SIZE;
            List<AdminProductProjection> products = productReadRepository.findAdminProducts(
                    emptyToNull(request.getQuery()),
                    productStatusForQuery(request.getStatus()),
                    emptyToNull(request.getCategory()),
                    page,
                    size);
            long total = productReadRepository.countAdminProducts(
                    emptyToNull(request.getQuery()),
                    productStatusForQuery(request.getStatus()),
                    emptyToNull(request.getCategory()));

            ProductPageResponse response = ProductPageResponse.newBuilder()
                    .addAllProducts(products.stream().map(this::toProductDto).toList())
                    .setCurrentPage(page + 1)
                    .setTotalElements(total)
                    .setTotalPages((int) Math.ceil((double) total / size))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get admin products", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getProductDetail(GetProductDetailRequest request, StreamObserver<ProductDto> responseObserver) {
        try {
            ProductDetailProjection detail = findProductDetail(request.getId());
            responseObserver.onNext(toProductDto(detail));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get product detail", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getProductStats(com.google.protobuf.Empty request, StreamObserver<ProductStatsResponse> responseObserver) {
        try {
            long active = productReadRepository.countProductsByStatus(ProductStatus.ACTIVE.name());
            long inactive = productReadRepository.countProductsByStatus(ProductStatus.INACTIVE.name());
            ProductStatsResponse response = ProductStatsResponse.newBuilder()
                    .setTotalProducts(active + inactive)
                    .setActiveProducts(active)
                    .setInactiveProducts(inactive)
                    .setLowStockProducts(productReadRepository.countLowStockProducts(LOW_STOCK_THRESHOLD))
                    .setOutOfStockProducts(productReadRepository.countOutOfStockProducts())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get product stats", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getLowStockProducts(GetLowStockProductsRequest request, StreamObserver<LowStockProductListResponse> responseObserver) {
        try {
            int threshold = request.getThreshold() > 0 ? request.getThreshold() : LOW_STOCK_THRESHOLD;
            int limit = request.getLimit() > 0 ? request.getLimit() : 5;
            LowStockProductListResponse response = LowStockProductListResponse.newBuilder()
                    .addAllProducts(productReadRepository.findLowStockProducts(threshold, limit).stream()
                            .map(this::toLowStockDto)
                            .toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get low stock products", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        complete(responseObserver, () -> createProductUseCase.execute(CreateProductCommand.builder()
                .categoryId(resolveCategoryId(request.getCategoryId(), request.getCategory()))
                .name(request.getName())
                .slug(resolveSlug(request.getSlug(), request.getName()))
                .description(defaultText(request.getDescription(), request.getName()))
                .modelUrl(request.getModel3DUrl())
                .supports3d(!request.getModel3DUrl().isBlank())
                .imageUrls(request.getImageUrlsList())
                .variants(resolveCreateVariants(request.getVariantsList(), request.getPrice(), request.getStock()))
                .build()), "Product created");
    }

    @Override
    @Transactional
    public void updateProduct(UpdateProductRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        complete(responseObserver, () -> {
            UUID productId = UUID.fromString(request.getId());
            updateProductInfoUseCase.execute(UpdateProductInfoCommand.builder()
                    .productId(productId)
                    .name(emptyToNull(request.getName()))
                    .slug(emptyToNull(resolveSlug(request.getSlug(), request.getName())))
                    .description(emptyToNull(request.getDescription()))
                    .modelUrl(emptyToNull(request.getModel3DUrl()))
                    .supports3d(!request.getModel3DUrl().isBlank())
                    .build());

            UUID categoryId = resolveCategoryId(request.getCategoryId(), request.getCategory());
            if (categoryId != null) {
                changeProductCategoryUseCase.execute(ChangeProductCategoryCommand.builder()
                        .productId(productId)
                        .categoryId(categoryId)
                        .build());
            }

            ProductStatus status = productStatusForCommand(request.getStatus());
            if (status != null) {
                updateProductStatusUseCase.execute(UpdateProductStatusCommand.builder()
                        .productId(productId)
                        .status(status)
                        .build());
            }

            replaceVariants(productId, request.getVariantsList(), request.getPrice(), request.getStock());
            replaceGallery(productId, request.getImageUrlsList());
        }, "Product updated");
    }

    @Override
    public void stockInVariant(StockInVariantRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        complete(responseObserver, () -> releaseInventoryUseCase.execute(UpdateInventoryCommand.builder()
                .orderCode(defaultText(request.getNote(), "ADMIN_STOCK_IN"))
                .items(List.of(UpdateInventoryCommand.StockItem.builder()
                        .productId(UUID.fromString(request.getProductId()))
                        .variantId(UUID.fromString(request.getVariantId()))
                        .quantity(Math.max(request.getQuantity(), 0))
                        .build()))
                .build()), "Variant stock updated");
    }

    @Override
    public void deleteProduct(DeleteProductRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        complete(responseObserver, () -> updateProductStatusUseCase.execute(UpdateProductStatusCommand.builder()
                .productId(UUID.fromString(request.getId()))
                .status(ProductStatus.INACTIVE)
                .build()), "Product deactivated");
    }

    @Override
    public void getAdminCategories(GetAdminCategoriesRequest request, StreamObserver<CategoryListResponse> responseObserver) {
        try {
            String query = emptyToNull(request.getQuery());
            List<CategoryDto> categories = categoryReadRepository.findAllCategories().stream()
                    .filter(category -> query == null
                            || category.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                            || category.getSlug().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                    .map(this::toCategoryDto)
                    .toList();
            responseObserver.onNext(CategoryListResponse.newBuilder().addAllCategories(categories).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get admin categories", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getCategoryDetail(GetCategoryDetailRequest request, StreamObserver<CategoryDto> responseObserver) {
        try {
            CategoryDetailProjection category = categoryReadRepository.findCategoryDetailById(UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            responseObserver.onNext(toCategoryDto(category));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get category detail", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getCategoryStats(com.google.protobuf.Empty request, StreamObserver<CategoryStatsResponse> responseObserver) {
        try {
            long total = categoryReadRepository.countCategories();
            responseObserver.onNext(CategoryStatsResponse.newBuilder()
                    .setTotalCategories(total)
                    .setVisibleCategories(total)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get category stats", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void createCategory(CreateCategoryRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        complete(responseObserver, () -> createCategoryUseCase.execute(CreateCategoryCommand.builder()
                .name(request.getName())
                .slug(resolveSlug(request.getSlug(), request.getName()))
                .parentId(parseOptionalUuid(request.getParentId()))
                .iconId(emptyToNull(request.getIconId()))
                .visible(request.getVisible())
                .description(emptyToNull(request.getDescription()))
                .imageUrl(emptyToNull(request.getImageUrl()))
                .build()), "Category created");
    }

    @Override
    public void updateCategory(UpdateCategoryRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        complete(responseObserver, () -> updateCategoryUseCase.execute(UpdateCategoryCommand.builder()
                .categoryId(UUID.fromString(request.getId()))
                .name(request.getName())
                .slug(resolveSlug(request.getSlug(), request.getName()))
                .parentId(parseOptionalUuid(request.getParentId()))
                .iconId(emptyToNull(request.getIconId()))
                .visible(request.getVisible())
                .description(emptyToNull(request.getDescription()))
                .imageUrl(emptyToNull(request.getImageUrl()))
                .build()), "Category updated");
    }

    @Override
    public void deleteCategory(DeleteCategoryRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        responseObserver.onNext(AdminActionResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Category delete is not supported until catalog-service has a delete use case")
                .build());
        responseObserver.onCompleted();
    }

    private ProductDto toProductDto(AdminProductProjection product) {
        return ProductDto.newBuilder()
                .setId(product.getId().toString())
                .setName(safe(product.getName()))
                .setSlug(safe(product.getSlug()))
                .setSku(safe(product.getSku()))
                .setCategory(safe(product.getCategoryName()))
                .setPrice(product.getPrice() == null ? 0D : product.getPrice())
                .setStock(product.getStock() == null ? 0 : product.getStock())
                .setStatus(toProductTone(product.getStatus(), product.getStock()))
                .setStatusLabel(toProductStatusLabel(product.getStatus(), product.getStock()))
                .setModel3DUrl(safe(product.getModelUrl()))
                .addAllImageUrls(product.getImageUrls() == null ? List.of() : product.getImageUrls())
                .addAllVariants(fetchAdminVariantDtos(product.getId()))
                .build();
    }

    private ProductDto toProductDto(ProductDetailProjection product) {
        int stock = product.getVariants() == null ? 0 : product.getVariants().stream()
                .mapToInt(variant -> variant.getStockQuantity() == null ? 0 : variant.getStockQuantity())
                .sum();
        double price = product.getPrice() == null ? 0D : product.getPrice();
        String category = product.getCategory() == null ? "" : product.getCategory().getLabel();
        return ProductDto.newBuilder()
                .setId(product.getId().toString())
                .setName(safe(product.getName()))
                .setSlug(safe(product.getSlug()))
                .setSku(safe(product.getSlug()))
                .setCategory(safe(category))
                .setPrice(price)
                .setStock(stock)
                .setStatus(toProductTone(product.getStatus(), stock))
                .setStatusLabel(toProductStatusLabel(product.getStatus(), stock))
                .setModel3DUrl(safe(product.getModelUrl()))
                .addAllImageUrls(product.getGallery() == null ? List.of() : product.getGallery())
                .addAllVariants(product.getVariants() == null ? List.of() : product.getVariants().stream()
                        .map(this::toVariantDto)
                        .toList())
                .build();
    }

    private List<CreateProductCommand.VariantCommand> resolveCreateVariants(
            List<ProductVariantInput> variants,
            double fallbackPrice,
            int fallbackStock) {
        if (variants != null && !variants.isEmpty()) {
            return variants.stream().map(this::toCreateVariantCommand).toList();
        }
        return List.of(CreateProductCommand.VariantCommand.builder()
                .price(Math.max(fallbackPrice, 0D))
                .stockQuantity(Math.max(fallbackStock, 0))
                .weight(1D)
                .length(1D)
                .width(1D)
                .height(1D)
                .material("N/A")
                .color("")
                .warranty("")
                .sku("")
                .build());
    }

    private CreateProductCommand.VariantCommand toCreateVariantCommand(ProductVariantInput variant) {
        return CreateProductCommand.VariantCommand.builder()
                .price(Math.max(variant.getPrice(), 0D))
                .stockQuantity(Math.max(variant.getStock(), 0))
                .weight(positiveOrDefault(variant.getWeight()))
                .length(positiveOrDefault(variant.getLength()))
                .width(positiveOrDefault(variant.getWidth()))
                .height(positiveOrDefault(variant.getHeight()))
                .material(defaultText(variant.getMaterial(), "N/A"))
                .warranty(safe(variant.getWarranty()))
                .color(safe(variant.getColor()))
                .sku(safe(variant.getSku()))
                .build();
    }

    private void replaceVariants(UUID productId, List<ProductVariantInput> variants, double fallbackPrice, int fallbackStock) {
        productRepository.findById(productId).ifPresent(product -> {
            if (variants != null && !variants.isEmpty()) {
                if (product.getVariants() != null) {
                    product.getVariants().clear();
                }
                variants.forEach(input -> product.addVariant(toProductVariant(input)));
            } else if (fallbackPrice >= 0 && fallbackStock >= 0 && product.getVariants() != null && !product.getVariants().isEmpty()) {
                ProductVariant primaryVariant = product.getVariants().get(0);
                primaryVariant.setPrice(new Price(BigDecimal.valueOf(fallbackPrice)));
                primaryVariant.setStockQuantity(new StockQuantity(fallbackStock));
            }
            productRepository.save(product);
        });
    }

    private ProductVariant toProductVariant(ProductVariantInput input) {
        UUID variantId = parseOptionalUuid(input.getId());
        return ProductVariant.builder()
                .id(variantId == null ? UUID.randomUUID() : variantId)
                .price(new Price(BigDecimal.valueOf(Math.max(input.getPrice(), 0D))))
                .stockQuantity(new StockQuantity(Math.max(input.getStock(), 0)))
                .dimensions(new ProductDimensions(
                        positiveOrDefault(input.getWeight()),
                        positiveOrDefault(input.getLength()),
                        positiveOrDefault(input.getWidth()),
                        positiveOrDefault(input.getHeight())))
                .material(defaultText(input.getMaterial(), "N/A"))
                .warranty(safe(input.getWarranty()))
                .color(safe(input.getColor()))
                .sku(safe(input.getSku()))
                .build();
    }

    private List<ProductVariantDto> fetchAdminVariantDtos(UUID productId) {
        List<ProductDetailProjection.VariantDto> variants = findProductDetail(productId.toString()).getVariants();
        if (variants == null) {
            return List.of();
        }
        return variants.stream()
                .map(this::toVariantDto)
                .toList();
    }

    private ProductVariantDto toVariantDto(ProductDetailProjection.VariantDto variant) {
        return ProductVariantDto.newBuilder()
                .setId(variant.getId() == null ? "" : variant.getId().toString())
                .setSku(safe(variant.getSku()))
                .setPrice(variant.getPrice() == null ? 0D : variant.getPrice())
                .setStock(variant.getStockQuantity() == null ? 0 : variant.getStockQuantity())
                .setColor(safe(variant.getColor()))
                .setMaterial(safe(variant.getMaterial()))
                .setWarranty(safe(variant.getWarranty()))
                .setWeight(variant.getWeight() == null ? 0D : variant.getWeight())
                .setLength(variant.getLength() == null ? 0D : variant.getLength())
                .setWidth(variant.getWidth() == null ? 0D : variant.getWidth())
                .setHeight(variant.getHeight() == null ? 0D : variant.getHeight())
                .setLabel(variantLabel(variant))
                .build();
    }

    private String variantLabel(ProductDetailProjection.VariantDto variant) {
        String color = safe(variant.getColor());
        String material = safe(variant.getMaterial());
        if (!color.isBlank() && !material.isBlank()) {
            return color + " / " + material;
        }
        if (!color.isBlank()) {
            return color;
        }
        if (!material.isBlank()) {
            return material;
        }
        return variant.getId() == null ? "Variant" : variant.getId().toString();
    }

    private void replaceGallery(UUID productId, List<String> imageUrls) {
        productRepository.findById(productId).ifPresent(product -> {
            if (product.getGallery() != null) {
                product.getGallery().clear();
            }
            if (imageUrls != null) {
                int position = 0;
                for (String imageUrl : imageUrls) {
                    if (imageUrl == null || imageUrl.isBlank()) {
                        continue;
                    }
                    product.addImage(ProductImage.builder()
                            .id(UUID.randomUUID())
                            .imageUrl(imageUrl.trim())
                            .position(position++)
                            .build());
                }
            }
            productRepository.save(product);
        });
    }

    private LowStockProductDto toLowStockDto(LowStockProductProjection product) {
        int stock = product.getStock() == null ? 0 : product.getStock();
        return LowStockProductDto.newBuilder()
                .setId(product.getId().toString())
                .setName(safe(product.getName()))
                .setCategory(safe(product.getCategoryName()))
                .setStock(stock)
                .setLevel(stock <= 0 ? "empty" : "low")
                .build();
    }

    private CategoryDto toCategoryDto(CategoryDetailProjection category) {
        return CategoryDto.newBuilder()
                .setId(category.getId().toString())
                .setName(safe(category.getName()))
                .setSlug(safe(category.getSlug()))
                .setProductCount(category.getProductCount() == null ? 0 : category.getProductCount())
                .setVisible(category.getVisible() == null || category.getVisible())
                .setVisibleLabel(category.getVisible() == null || category.getVisible() ? "Hiển thị" : "Ẩn")
                .setCreatedAt("")
                .setIconId(resolveIconId(category.getIconUrl()))
                .setDescription(safe(category.getDescription()))
                .setImageUrl(safe(category.getImageUrl()))
                .build();
    }

    private ProductDetailProjection findProductDetail(String idOrSlug) {
        try {
            return productReadRepository.findProductDetailById(UUID.fromString(idOrSlug))
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        } catch (IllegalArgumentException ignored) {
            return productReadRepository.findProductDetailBySlug(idOrSlug)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        }
    }

    private UUID resolveCategoryId(String categoryId, String categoryNameOrSlug) {
        UUID parsed = parseOptionalUuid(categoryId);
        if (parsed != null) {
            return parsed;
        }
        String normalized = emptyToNull(categoryNameOrSlug);
        if (normalized == null) {
            return null;
        }
        return categoryReadRepository.findAllCategories().stream()
                .filter(category -> normalized.equalsIgnoreCase(category.getName())
                        || normalized.equalsIgnoreCase(category.getSlug())
                        || normalized.equalsIgnoreCase(category.getId().toString()))
                .map(CategoryDetailProjection::getId)
                .findFirst()
                .orElse(null);
    }

    private ProductStatus productStatusForCommand(String rawStatus) {
        if (rawStatus != null) {
            String lower = rawStatus.trim().toLowerCase(Locale.ROOT);
            if (lower.contains("còn") || lower.contains("sap") || lower.contains("sắp")) {
                return ProductStatus.ACTIVE;
            }
            if (lower.contains("hết") || lower.contains("ngừng")) {
                return ProductStatus.INACTIVE;
            }
        }
        String normalized = normalizeStatus(rawStatus);
        if (normalized.isBlank()) {
            return null;
        }
        if ("LOW".equals(normalized) || "SUCCESS".equals(normalized) || "CON_HANG".equals(normalized)) {
            return ProductStatus.ACTIVE;
        }
        if ("CANCEL".equals(normalized) || "HET_HANG".equals(normalized)) {
            return ProductStatus.INACTIVE;
        }
        return ProductStatus.valueOf(normalized);
    }

    private String productStatusForQuery(String rawStatus) {
        ProductStatus status = productStatusForCommand(rawStatus);
        return status == null ? null : status.name();
    }

    private String toProductTone(String status, Integer stock) {
        if ("INACTIVE".equalsIgnoreCase(status)) {
            return "cancel";
        }
        if (stock != null && stock <= LOW_STOCK_THRESHOLD) {
            return stock <= 0 ? "cancel" : "low";
        }
        return "success";
    }

    private String toProductStatusLabel(String status, Integer stock) {
        if ("INACTIVE".equalsIgnoreCase(status)) {
            return "Ngừng bán";
        }
        if (stock != null && stock <= 0) {
            return "Hết hàng";
        }
        if (stock != null && stock <= LOW_STOCK_THRESHOLD) {
            return "Sắp hết";
        }
        return "Còn hàng";
    }

    private void complete(StreamObserver<AdminActionResponse> responseObserver, Runnable action, String message) {
        try {
            action.run();
            responseObserver.onNext(AdminActionResponse.newBuilder().setSuccess(true).setMessage(message).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Admin catalog action failed", ex);
            responseObserver.onError(ex);
        }
    }

    private UUID parseOptionalUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return UUID.fromString(raw);
    }

    private String resolveSlug(String slug, String name) {
        String value = emptyToNull(slug);
        if (value != null) {
            return value;
        }
        String source = defaultText(name, "item");
        return source.trim().toLowerCase(Locale.ROOT)
                .transform(AdminCatalogGrpcServer::stripAccents)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    private static String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    private String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.trim()
                .replace(' ', '_')
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
    }

    private String resolveIconId(String iconUrl) {
        String value = emptyToNull(iconUrl);
        return value == null ? "box" : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private double positiveOrDefault(double value) {
        return value > 0 ? value : 1D;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
