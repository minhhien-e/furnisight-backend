package com.furnisight.admin.catalog.product.application;

import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.ProductVariantDto;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @Test
    void mapsVariantWithoutProductLevelModelFields() {
        AdminCatalogGrpcClient client = mock(AdminCatalogGrpcClient.class);
        when(client.getProductDetail("product-1"))
                .thenReturn(ProductDto.newBuilder()
                        .setId("product-1")
                        .setName("Chair")
                        .setSku("CHAIR")
                        .setCategory("Living room")
                        .addVariants(ProductVariantDto.newBuilder()
                                .setId("variant-1")
                                .setSku("CHAIR-BLUE")
                                .setPrice(100)
                                .setStock(3)
                                .setLowStockThreshold(5)
                                .build())
                        .build());

        var response = new ProductService(client, new ProductValidator()).getProduct("product-1");

        assertThat(response.variants()).singleElement().satisfies(variant -> {
            assertThat(variant.sku()).isEqualTo("CHAIR-BLUE");
            assertThat(variant.lowStockThreshold()).isEqualTo(5);
            assertThat(variant.modelUrl()).isEmpty();
            assertThat(variant.imageUrls()).isEmpty();
        });
    }
}
