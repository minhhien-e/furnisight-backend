package com.furnisight.admin.catalog.product.application;

import com.furnisight.admin.catalog.CreateProductRequest;
import com.furnisight.admin.catalog.ProductDto;
import com.furnisight.admin.catalog.ProductVariantDto;
import com.furnisight.admin.catalog.ProductVariantInput;
import com.furnisight.admin.catalog.UpdateProductRequest;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.catalog.product.web.dto.request.UpsertProductRequest;
import com.furnisight.admin.catalog.product.web.dto.request.UpsertProductVariantRequest;
import com.furnisight.admin.catalog.product.web.dto.response.ProductResponse;
import com.furnisight.admin.catalog.product.web.dto.response.ProductVariantResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final AdminCatalogGrpcClient catalogClient;
    private final ProductValidator validator;

    public PageResponse<ProductResponse> getProducts(int page, int size, String query, String status, String category) {
        com.furnisight.admin.catalog.ProductPageResponse response =
                catalogClient.getProducts(page, size, query, status, category);
        return new PageResponse<>(
                response.getProductsList().stream().map(this::toProductResponse).toList(),
                response.getTotalPages(),
                response.getTotalElements(),
                response.getCurrentPage());
    }

    public ProductResponse getProduct(String id) {
        return toProductResponse(catalogClient.getProductDetail(id));
    }

    public ActionResultResponse createProduct(UpsertProductRequest request) {
        validator.validateVariants(request.variants());
        return toActionResult(catalogClient.createProduct(CreateProductRequest.newBuilder()
                .setName(value(request.name()))
                .setSlug(slugFrom(request.sku(), request.name()))
                .setCategory(value(request.category()))
                .setPrice(request.price())
                .setStock(request.stock())
                .setSku(value(request.sku()))
                .setStatus(productStatusInput(request))
                .setDescription(value(request.description()))
                .setModelMediaId(value(request.modelMediaId()))
                .setModelUrl(value(request.modelUrl()))
                .setSupports3D(request.supports3d())
                .addAllImageUrls(cleanList(request.imageUrls()))
                .addAllVariants(toVariantInputs(request.variants()))
                .build()));
    }

    public ActionResultResponse updateProduct(String id, UpsertProductRequest request) {
        validator.validateVariants(request.variants());
        return toActionResult(catalogClient.updateProduct(UpdateProductRequest.newBuilder()
                .setId(value(id))
                .setName(value(request.name()))
                .setSlug(slugFrom(request.sku(), request.name()))
                .setCategory(value(request.category()))
                .setPrice(request.price())
                .setStock(request.stock())
                .setSku(value(request.sku()))
                .setStatus(productStatusInput(request))
                .setDescription(value(request.description()))
                .setModelMediaId(value(request.modelMediaId()))
                .setModelUrl(value(request.modelUrl()))
                .setSupports3D(request.supports3d())
                .addAllImageUrls(cleanList(request.imageUrls()))
                .addAllVariants(toVariantInputs(request.variants()))
                .build()));
    }

    public ActionResultResponse deleteProduct(String id) {
        return toActionResult(catalogClient.deleteProduct(id));
    }

    private ProductResponse toProductResponse(ProductDto product) {
        return new ProductResponse(
                product.getId(), product.getName(), product.getSku(), product.getCategory(),
                product.getPrice(), product.getStock(), product.getStatus(), product.getStatusLabel(),
                product.getModelMediaId(), product.getModelUrl(), product.getSupports3D(),
                product.getModel3DFileName(), product.getModel3DSize(), product.getImageUrlsList(),
                product.getVariantsList().stream().map(this::toVariantResponse).toList());
    }

    private ProductVariantResponse toVariantResponse(ProductVariantDto variant) {
        return new ProductVariantResponse(
                variant.getId(), variant.getSku(), variant.getPrice(), variant.getStock(),
                variant.getColor(), variant.getMaterial(), variant.getWarranty(), variant.getWeight(),
                variant.getLength(), variant.getWidth(), variant.getHeight(), variant.getLabel(),
                variant.getLowStockThreshold());
    }

    private List<ProductVariantInput> toVariantInputs(List<UpsertProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            return List.of();
        }
        return variants.stream().map(this::toVariantInput).toList();
    }

    private ProductVariantInput toVariantInput(UpsertProductVariantRequest variant) {
        return ProductVariantInput.newBuilder()
                .setId(value(variant.id()))
                .setSku(validator.normalizeSku(variant.sku()))
                .setPrice(variant.price())
                .setStock(variant.stock())
                .setColor(value(variant.color()))
                .setMaterial(value(variant.material()))
                .setWarranty(value(variant.warranty()))
                .setWeight(variant.weight())
                .setLength(variant.length())
                .setWidth(variant.width())
                .setHeight(variant.height())
                .setLowStockThreshold(validator.validThreshold(variant.lowStockThreshold()))
                .build();
    }

    private ActionResultResponse toActionResult(com.furnisight.admin.catalog.AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private String productStatusInput(UpsertProductRequest request) {
        return request.status() != null && !request.status().isBlank()
                ? request.status() : value(request.statusLabel());
    }

    private String slugFrom(String sku, String name) {
        if (sku != null && !sku.isBlank()) {
            return sku;
        }
        if (name == null || name.isBlank()) {
            return "";
        }
        return stripAccents(name.trim().toLowerCase())
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

    private String value(String value) {
        return value == null ? "" : value;
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }
}
