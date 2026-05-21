package com.furnisight.catalog.presentation.grpc;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.GetProductSummaryItem;
import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummariesResponse;
import com.furnisight.catalog.ProductSummary;
import com.furnisight.catalog.ProductSummaryVariant;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
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

    @Override
    public void getProductSummaries(
            GetProductSummariesRequest request,
            StreamObserver<GetProductSummariesResponse> responseObserver
    ) {
        try {
            List<ProductSummary> products = resolveRequestItems(request).stream()
                    .map(item -> productReadRepository.findProductDetailById(item.productId())
                            .map(detail -> toProductSummary(detail, item.selectedVariantId())))
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

    private ProductSummary toProductSummary(ProductDetailProjection detail, String selectedVariantId) {
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

    private List<ProductDetailProjection.VariantDto> orderedVariants(
            ProductDetailProjection detail,
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

    private ProductSummaryVariant toProductSummaryVariant(ProductDetailProjection.VariantDto variant) {
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

        return variantBuilder.build();
    }

    private boolean matchesSelectedVariant(ProductDetailProjection.VariantDto variant, String selectedVariantId) {
        return variant.getId() != null && variant.getId().toString().equals(selectedVariantId);
    }

    private String resolveSelectedVariantId(String selectedVariantId, List<ProductSummaryVariant> variants) {
        if (selectedVariantId != null && !selectedVariantId.isBlank()) {
            return selectedVariantId;
        }

        return variants.isEmpty() ? "" : defaultString(variants.get(0).getId());
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private record SummaryRequestItem(UUID productId, String selectedVariantId) {
    }
}
