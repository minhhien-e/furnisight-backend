package com.furnisight.cart.service;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.GetProductSummariesRequest;
import com.furnisight.catalog.GetProductSummariesResponse;
import com.furnisight.catalog.ProductSummary;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class CatalogGrpcClient {

    @GrpcClient("catalog-service")
    private Channel catalogChannel;

    public Map<String, ProductSummary> getProductSummaries(Collection<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        GetProductSummariesRequest request = GetProductSummariesRequest.newBuilder()
                .addAllProductIds(productIds)
                .build();

        CatalogServiceGrpc.CatalogServiceBlockingStub stub = CatalogServiceGrpc.newBlockingStub(catalogChannel);
        GetProductSummariesResponse response = stub.getProductSummaries(request);

        Map<String, ProductSummary> productMap = new LinkedHashMap<>();
        for (ProductSummary product : response.getProductsList()) {
            productMap.put(product.getId(), product);
        }
        return productMap;
    }
}
