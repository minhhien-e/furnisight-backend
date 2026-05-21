package com.furnisight.catalog.presentation.grpc;

import com.furnisight.catalog.CatalogServiceGrpc;
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
            List<ProductSummary> products = request.getProductIdsList().stream()
                    .map(this::parseProductId)
                    .map(productReadRepository::findProductDetailById)
                    .flatMap(java.util.Optional::stream)
                    .map(this::toProductSummary)
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

    private UUID parseProductId(String rawProductId) {
        try {
            return UUID.fromString(rawProductId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid product id: " + rawProductId, ex);
        }
    }

    private ProductSummary toProductSummary(ProductDetailProjection detail) {
        ProductSummary.Builder builder = ProductSummary.newBuilder()
                .setId(detail.getId().toString())
                .setSlug(defaultString(detail.getSlug()))
                .setName(defaultString(detail.getName()));

        if (detail.getGallery() != null && !detail.getGallery().isEmpty()) {
            builder.setImage(defaultString(detail.getGallery().get(0)));
        }

        ProductDetailProjection.VariantDto variant = firstVariant(detail);
        if (variant != null) {
            ProductSummaryVariant.Builder variantBuilder = ProductSummaryVariant.newBuilder()
                    .setId(variant.getId() != null ? variant.getId().toString() : "")
                    .setColor(defaultString(variant.getColor()));

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

            builder.setVariant(variantBuilder.build());
        }

        return builder.build();
    }

    private ProductDetailProjection.VariantDto firstVariant(ProductDetailProjection detail) {
        if (detail.getVariants() == null || detail.getVariants().isEmpty()) {
            return null;
        }
        return detail.getVariants().get(0);
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }
}
