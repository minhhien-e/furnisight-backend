package com.furnisight.catalog.presentation.grpc;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.FavoriteProductSummary;
import com.furnisight.catalog.GetFavoriteProductSummariesRequest;
import com.furnisight.catalog.GetFavoriteProductSummariesResponse;
import com.furnisight.catalog.GetProductSummaryItem;
import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummariesResponse;
import com.furnisight.catalog.ProductSummary;
import com.furnisight.catalog.ProductSummaryVariant;
import com.furnisight.catalog.RecommendedProduct;
import com.furnisight.catalog.SearchRecommendedProductsRequest;
import com.furnisight.catalog.SearchRecommendedProductsResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.application.product.service.ProductTranslationService;
import com.furnisight.catalog.application.product.service.ProductReviewStatsEnricher;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Comparator;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcCatalogService extends CatalogServiceGrpc.CatalogServiceImplBase {

    private final ProductReadRepository productReadRepository;
    private final ProductTranslationService productTranslationService;
    private final ProductReviewStatsEnricher productReviewStatsEnricher;

    @Override
    public void getProductSummaries(
            GetProductSummariesRequest request,
            StreamObserver<GetProductSummariesResponse> responseObserver
    ) {
        try {
            String locale = productTranslationService.normalizeLang(request.getLocale());
            List<ProductSummary> products = resolveRequestItems(request).stream()
                    .map(item -> productReadRepository.findProductDetailById(item.productId())
                            .map(detail -> toProductSummary(localizeDetail(detail, locale), item.selectedVariantId())))
                    .flatMap(java.util.Optional::stream)
                    .toList();

            responseObserver.onNext(
                    GetProductSummariesResponse.newBuilder()
                            .addAllProducts(products)
                            .build()
            );
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(ex.getMessage())
                            .withCause(ex)
                            .asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(ex.getMessage())
                            .withCause(ex)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getFavoriteProductSummaries(
            GetFavoriteProductSummariesRequest request,
            StreamObserver<GetFavoriteProductSummariesResponse> responseObserver
    ) {
        try {
            String locale = productTranslationService.normalizeLang(request.getLocale());
            List<FavoriteProductSummary> products = request.getProductIdsList().stream()
                    .map(this::parseProductId)
                    .distinct()
                    .map(productId -> productReadRepository.findProductDetailById(productId)
                            .map(detail -> toFavoriteProductSummary(localizeDetail(detail, locale))))
                    .flatMap(java.util.Optional::stream)
                    .toList();

            responseObserver.onNext(
                    GetFavoriteProductSummariesResponse.newBuilder()
                            .addAllProducts(products)
                            .build()
            );
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(ex.getMessage())
                            .withCause(ex)
                            .asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(ex.getMessage())
                            .withCause(ex)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void searchRecommendedProducts(
            SearchRecommendedProductsRequest request,
            StreamObserver<SearchRecommendedProductsResponse> responseObserver) {
        try {
            String categorySlug = request.getCategorySlug() == null
                    ? "" : request.getCategorySlug().trim();
            if (categorySlug.isBlank()) {
                throw new IllegalArgumentException("category_slug is required");
            }
            int limit = request.getLimit() > 0 ? Math.min(request.getLimit(), 50) : 6;
            String status = request.getStatus().isBlank() ? "ACTIVE" : request.getStatus();

            responseObserver.onNext(SearchRecommendedProductsResponse.newBuilder()
                    .addAllProducts(productReadRepository
                            .findRecommendedProducts(categorySlug, status, limit)
                            .stream()
                            .collect(java.util.stream.Collectors.collectingAndThen(
                                    java.util.stream.Collectors.toList(),
                                    productReviewStatsEnricher::enrichAll
                            ))
                            .stream()
                            .map(this::toRecommendedProduct)
                            .toList())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getMessage())
                    .withCause(ex)
                    .asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(ex.getMessage())
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    private List<SummaryRequestItem> resolveRequestItems(GetProductSummariesRequest request) {
        if (request.getItemsCount() > 0) {
            return request.getItemsList().stream()
                    .map(this::toSummaryRequestItem)
                    .toList();
        }

        return request.getProductIdsList().stream()
                .map(productId -> new SummaryRequestItem(parseProductId(productId), ""))
                .toList();
    }

    private SummaryRequestItem toSummaryRequestItem(GetProductSummaryItem item) {
        return new SummaryRequestItem(
                parseProductId(item.getProductId()),
                defaultString(item.getSelectedVariantId())
        );
    }

    private UUID parseProductId(String rawProductId) {
        try {
            return UUID.fromString(rawProductId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid product id: " + rawProductId, ex);
        }
    }

    private ProductSummary toProductSummary(ProductResponse detail, String selectedVariantId) {
        ProductSummary.Builder builder = ProductSummary.newBuilder()
                .setId(detail.getId().toString())
                .setSlug(defaultString(detail.getSlug()))
                .setName(defaultString(detail.getName()))
                .setSelectedVariantId(defaultString(selectedVariantId));

        if (detail.getGallery() != null && !detail.getGallery().isEmpty()) {
            builder.setImage(defaultString(detail.getGallery().get(0)));
        }

        List<ProductSummaryVariant> variants = orderedVariants(detail, selectedVariantId).stream()
                .map(this::toProductSummaryVariant)
                .toList();

        if (!variants.isEmpty()) {
            builder.setSelectedVariantId(resolveSelectedVariantId(selectedVariantId, variants));
            builder.setVariant(variants.get(0));
            builder.addAllVariants(variants);
        }

        return builder.build();
    }

    private FavoriteProductSummary toFavoriteProductSummary(ProductResponse detail) {
        FavoriteProductSummary.Builder builder = FavoriteProductSummary.newBuilder()
                .setId(detail.getId().toString())
                .setSlug(defaultString(detail.getSlug()))
                .setName(defaultString(detail.getName()))
                .setSoldCount(detail.getSoldCount() != null ? detail.getSoldCount() : 0);

        if (detail.getGallery() != null && !detail.getGallery().isEmpty()) {
            builder.setImage(defaultString(detail.getGallery().get(0)));
        }
        if (detail.getCategory() != null) {
            builder.setCategoryName(defaultString(detail.getCategory().getLabel()));
        }
        if (detail.getPrice() != null) {
            builder.setPrice(detail.getPrice());
        }
        return builder.build();
    }

    private List<ProductResponse.VariantDto> orderedVariants(
            ProductResponse detail,
            String selectedVariantId
    ) {
        if (detail.getVariants() == null || detail.getVariants().isEmpty()) {
            return List.of();
        }

        if (selectedVariantId == null || selectedVariantId.isBlank()) {
            return List.copyOf(detail.getVariants());
        }

        return detail.getVariants().stream()
                .sorted(Comparator.comparingInt(variant -> matchesSelectedVariant(variant, selectedVariantId) ? 0 : 1))
                .toList();
    }

    private ProductSummaryVariant toProductSummaryVariant(ProductResponse.VariantDto variant) {
        ProductSummaryVariant.Builder variantBuilder = ProductSummaryVariant.newBuilder()
                .setId(variant.getId() != null ? variant.getId().toString() : "")
                .setColor(defaultString(variant.getColor()))
                .setMaterial(defaultString(variant.getMaterial()))
                .setWarranty(defaultString(variant.getWarranty()));

        if (variant.getPrice() != null) {
            variantBuilder.setPrice(variant.getPrice());
        }
        if (variant.getStockQuantity() != null) {
            variantBuilder.setStockQuantity(variant.getStockQuantity());
        }
        if (variant.getLength() != null) {
            variantBuilder.setLength(variant.getLength());
        }
        if (variant.getWidth() != null) {
            variantBuilder.setWidth(variant.getWidth());
        }
        if (variant.getHeight() != null) {
            variantBuilder.setHeight(variant.getHeight());
        }
        if (variant.getWeight() != null) {
            variantBuilder.setWeight(variant.getWeight());
        }
        if (variant.getModelUrl() != null) {
            variantBuilder.setModelUrl(variant.getModelUrl());
        }
        if (variant.getImageUrls() != null) {
            variantBuilder.addAllImageUrls(variant.getImageUrls());
        }
        if (variant.getSupports3d() != null) {
            variantBuilder.setSupports3D(variant.getSupports3d());
        }

        return variantBuilder.build();
    }

    private boolean matchesSelectedVariant(ProductResponse.VariantDto variant, String selectedVariantId) {
        return variant.getId() != null && variant.getId().toString().equals(selectedVariantId);
    }

    private String resolveSelectedVariantId(String selectedVariantId, List<ProductSummaryVariant> variants) {
        if (selectedVariantId != null && !selectedVariantId.isBlank()
                && variants.stream().anyMatch(variant -> variant.getId().equals(selectedVariantId))) {
            return selectedVariantId;
        }

        return variants.isEmpty() ? "" : defaultString(variants.get(0).getId());
    }

    private RecommendedProduct toRecommendedProduct(ProductResponse product) {
        RecommendedProduct.Builder builder = RecommendedProduct.newBuilder()
                .setId(product.getId().toString())
                .setSlug(defaultString(product.getSlug()))
                .setName(defaultString(product.getName()))
                .setCategoryName(defaultString(product.getCategoryName()))
                .setImage(defaultString(product.getImage()))
                .setDefaultVariantId(product.getDefaultVariantId() == null
                        ? "" : product.getDefaultVariantId().toString())
                .setRating(product.getRating() == null ? 0D : product.getRating())
                .setRatingCount(product.getRatingCount() == null ? 0 : product.getRatingCount())
                .setSoldCount(product.getSoldCount() == null ? 0 : product.getSoldCount());

        if (product.getPrice() != null) {
            builder.setPrice(product.getPrice());
        }

        if (product.getVariants() != null) {
            List<ProductSummaryVariant> variants = product.getVariants().stream()
                    .map(this::toProductSummaryVariant)
                    .toList();
            builder.addAllVariants(variants);
        }

        return builder.build();
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private ProductResponse localizeDetail(ProductResponse detail, String locale) {
        return productTranslationService.localizeProduct(detail, locale);
    }

    private record SummaryRequestItem(UUID productId, String selectedVariantId) {
    }
}
