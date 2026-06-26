package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.product.dto.command.*;
import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.response.*;
import com.furnisight.catalog.application.product.port.in.usecase.*;
import com.furnisight.catalog.presentation.web.rest.dto.request.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductInfoUseCase updateProductInfoUseCase;
    private final AddProductVariantUseCase addProductVariantUseCase;
    private final RemoveProductVariantUseCase removeProductVariantUseCase;
    private final UpdateProductStatusUseCase updateProductStatusUseCase;
    private final GetProductDetailQueryUseCase getProductDetailQueryUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final GetTopProductsUseCase getTopProductsUseCase;
    private final ChangeProductCategoryUseCase changeProductCategoryUseCase;
    private final GetWeeklyFavoriteProductsUseCase getWeeklyFavoriteProductsUseCase;

    // ─── COMMANDS ────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody CreateProductRequest request) {
        CreateProductCommand command = CreateProductCommand.builder()
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .slug(request.getSlug())
                .sku(request.getSku())
                .description(request.getDescription())
                .features(request.getFeatures())
                .imageUrls(request.getImageUrls())
                .variants(java.util.Optional.ofNullable(request.getVariants()).orElse(List.of()).stream()
                        .map(v -> CreateProductCommand.VariantCommand.builder()
                                .price(v.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .weight(v.getWeight())
                                .length(v.getLength())
                                .width(v.getWidth())
                                .height(v.getHeight())
                                .material(v.getMaterial())
                                .color(v.getColor())
                                .warranty(v.getWarranty())
                                .sku(v.getSku())
                                .lowStockThreshold(v.getLowStockThreshold())
                                .modelMediaId(v.getModelMediaId())
                                .modelUrl(v.getModelUrl())
                                .supports3d(v.getSupports3d())
                                .imageUrls(v.getImageUrls())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        createProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(name = "productId", value = "/{productId}")
    public ResponseEntity<Void> updateProductInfo(@PathVariable(name = "productId") UUID productId,
            @RequestBody UpdateProductInfoRequest request) {
        UpdateProductInfoCommand command = UpdateProductInfoCommand.builder()
                .productId(productId)
                .name(request.getName())
                .slug(request.getSlug())
                .sku(request.getSku())
                .description(request.getDescription())
                .features(request.getFeatures())
                .build();

        updateProductInfoUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping(name = "productId", value = "/{productId}/category")
    public ResponseEntity<Void> changeCategory(@PathVariable(name = "productId") UUID productId,
            @RequestBody ChangeProductCategoryRequest request) {
        ChangeProductCategoryCommand command = ChangeProductCategoryCommand.builder()
                .productId(productId)
                .categoryId(request.getCategoryId())
                .build();
        changeProductCategoryUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping(name = "productId", value = "/{productId}/variants")
    public ResponseEntity<Void> addVariant(@PathVariable(name = "productId") UUID productId,
            @RequestBody AddProductVariantRequest request) {
        AddProductVariantCommand command = AddProductVariantCommand.builder()
                .productId(productId)
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .material(request.getMaterial())
                .color(request.getColor())
                .warranty(request.getWarranty())
                .sku(request.getSku())
                .lowStockThreshold(request.getLowStockThreshold())
                .modelMediaId(request.getModelMediaId())
                .modelUrl(request.getModelUrl())
                .supports3d(request.getSupports3d())
                .imageUrls(request.getImageUrls())
                .build();
        addProductVariantUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(name = "productId", value = "/{productId}/variants/{variantId}")
    public ResponseEntity<Void> removeVariant(@PathVariable(name = "productId") UUID productId,
            @PathVariable(name = "variantId") UUID variantId) {
        RemoveProductVariantCommand command = RemoveProductVariantCommand.builder()
                .productId(productId)
                .variantId(variantId)
                .build();
        removeProductVariantUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(name = "productId", value = "/{productId}/status")
    public ResponseEntity<Void> updateProductStatus(@PathVariable(name = "productId") UUID productId,
            @RequestBody UpdateProductStatusRequest request) {
        UpdateProductStatusCommand command = UpdateProductStatusCommand.builder()
                .productId(productId)
                .status(request.getStatus())
                .build();

        updateProductStatusUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    // ─── QUERIES ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "priceBands", required = false) List<String> priceBands,
            @RequestParam(name = "priceSliderPct", required = false) List<Double> priceSliderPct,
            @RequestParam(name = "materials", required = false) List<String> materials,
            @RequestParam(name = "colors", required = false) List<String> colors,
            @RequestParam(name = "minStar", required = false) Integer minStar,
            @RequestParam(name = "saleOnly", required = false) Boolean saleOnly,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "24") int size) {

        int backendPage = Math.max(0, page > 0 ? page - 1 : 0);

        SearchProductsQuery queryParam = SearchProductsQuery.builder()
                .q(q)
                .category(category)
                .sort(sort)
                .priceBands(priceBands)
                .priceSliderPct(priceSliderPct)
                .materials(materials)
                .colors(colors)
                .minStar(minStar)
                .saleOnly(saleOnly)
                .status(status)
                .page(backendPage)
                .size(size)
                .build();

        PageResponse<ProductResponse> results = searchProductsUseCase.execute(queryParam);
        return ResponseEntity.ok(results);
    }

    @GetMapping(name = "slug", value = "/{slug}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable(name = "slug") String slug) {
        GetProductDetailQuery query = new GetProductDetailQuery(slug);
        ProductResponse result = getProductDetailQueryUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top")
    public ResponseEntity<List<ProductResponse>> getTopProducts(
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<ProductResponse> results = getTopProductsUseCase.execute(limit);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/favorite-weekly")
    public ResponseEntity<List<ProductResponse>> getWeeklyFavoriteProducts(
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<ProductResponse> results = getWeeklyFavoriteProductsUseCase.execute(limit);
        return ResponseEntity.ok(results);
    }
}
