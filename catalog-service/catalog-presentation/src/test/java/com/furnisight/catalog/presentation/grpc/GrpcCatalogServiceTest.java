package com.furnisight.catalog.presentation.grpc;

import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummariesResponse;
import com.furnisight.catalog.GetProductSummaryItem;
import com.furnisight.catalog.RecommendedProduct;
import com.furnisight.catalog.SearchRecommendedProductsRequest;
import com.furnisight.catalog.SearchRecommendedProductsResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrpcCatalogServiceTest {

    @Test
    void recommendationKeepsPriceAndDefaultVariantWithoutProductModelUrl() {
        ProductReadRepository repository = mock(ProductReadRepository.class);
        UUID productId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        when(repository.findRecommendedProducts("bedroom", "ACTIVE", 6))
                .thenReturn(List.of(ProductResponse.builder()
                        .id(productId)
                        .slug("bed")
                        .name("Bed")
                        .categoryName("Bedroom")
                        .price(9_500_000D)
                        .defaultVariantId(variantId)
                        .variants(List.of(ProductResponse.VariantDto.builder()
                                .id(variantId)
                                .price(9_500_000D)
                                .modelUrl("https://example.com/bed.glb")
                                .build()))
                        .build()));

        CapturingObserver<SearchRecommendedProductsResponse> observer =
                new CapturingObserver<>();
        new GrpcCatalogService(repository).searchRecommendedProducts(
                SearchRecommendedProductsRequest.newBuilder()
                        .setCategorySlug("bedroom")
                        .setStatus("ACTIVE")
                        .setLimit(6)
                        .build(),
                observer);

        RecommendedProduct product = observer.value.getProducts(0);
        assertThat(product.hasPrice()).isTrue();
        assertThat(product.getPrice()).isEqualTo(9_500_000D);
        assertThat(product.getDefaultVariantId()).isEqualTo(variantId.toString());
        assertThat(product.getModelUrl()).isEmpty();
        assertThat(product.getVariants(0).getModelUrl()).endsWith(".glb");
    }

    @Test
    void recommendationWithoutVariantKeepsPriceAbsent() {
        ProductReadRepository repository = mock(ProductReadRepository.class);
        when(repository.findRecommendedProducts("bedroom", "ACTIVE", 6))
                .thenReturn(List.of(ProductResponse.builder()
                        .id(UUID.randomUUID())
                        .slug("bed")
                        .name("Bed")
                        .build()));

        CapturingObserver<SearchRecommendedProductsResponse> observer =
                new CapturingObserver<>();
        new GrpcCatalogService(repository).searchRecommendedProducts(
                SearchRecommendedProductsRequest.newBuilder()
                        .setCategorySlug("bedroom")
                        .setStatus("ACTIVE")
                        .setLimit(6)
                        .build(),
                observer);

        RecommendedProduct product = observer.value.getProducts(0);
        assertThat(product.hasPrice()).isFalse();
        assertThat(product.getDefaultVariantId()).isEmpty();
    }

    @Test
    void invalidSelectedVariantFallsBackToFirstVariant() {
        ProductReadRepository repository = mock(ProductReadRepository.class);
        UUID productId = UUID.randomUUID();
        UUID firstVariantId = UUID.randomUUID();
        when(repository.findProductDetailById(productId))
                .thenReturn(Optional.of(ProductResponse.builder()
                        .id(productId)
                        .slug("bed")
                        .name("Bed")
                        .variants(List.of(ProductResponse.VariantDto.builder()
                                .id(firstVariantId)
                                .price(100D)
                                .build()))
                        .build()));

        CapturingObserver<GetProductSummariesResponse> observer =
                new CapturingObserver<>();
        new GrpcCatalogService(repository).getProductSummaries(
                GetProductSummariesRequest.newBuilder()
                        .addItems(GetProductSummaryItem.newBuilder()
                                .setProductId(productId.toString())
                                .setSelectedVariantId(UUID.randomUUID().toString())
                                .build())
                        .build(),
                observer);

        assertThat(observer.value.getProducts(0).getSelectedVariantId())
                .isEqualTo(firstVariantId.toString());
    }

    private static class CapturingObserver<T> implements StreamObserver<T> {
        private T value;

        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable throwable) {
            throw new AssertionError(throwable);
        }

        @Override
        public void onCompleted() {
        }
    }
}
