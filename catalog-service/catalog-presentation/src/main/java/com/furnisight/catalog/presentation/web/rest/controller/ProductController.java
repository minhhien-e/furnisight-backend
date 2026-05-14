package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.product.dto.command.*;
import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
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
    private final UpdateProductVariantsUseCase updateProductVariantsUseCase;
    private final UpdateProductStatusUseCase updateProductStatusUseCase;
    private final GetProductDetailQueryUseCase getProductDetailQueryUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final GetTopProductsUseCase getTopProductsUseCase;

    // ─── COMMANDS ────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody CreateProductRequest request) {
        CreateProductCommand command = CreateProductCommand.builder()
                .shopId(request.getShopId())
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .description(request.getDescription())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .attributes(request.getAttributes())
                .variants(request.getVariants().stream()
                        .map(v -> CreateProductCommand.VariantCommand.builder()
                                .sku(v.getSku())
                                .price(v.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        createProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(name = "productId", value = "/{productId}")
    public ResponseEntity<Void> updateProductInfo(@PathVariable(name = "productId") UUID productId, @RequestBody UpdateProductInfoRequest request) {
        UpdateProductInfoCommand command = UpdateProductInfoCommand.builder()
                .productId(productId)
                .shopId(request.getShopId())
                .name(request.getName())
                .description(request.getDescription())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .attributes(request.getAttributes())
                .build();

        updateProductInfoUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping(name = "productId", value = "/{productId}/variants")
    public ResponseEntity<Void> updateProductVariants(@PathVariable(name = "productId") UUID productId, @RequestBody UpdateProductVariantsRequest request) {
        UpdateProductVariantsCommand command = UpdateProductVariantsCommand.builder()
                .productId(productId)
                .shopId(request.getShopId())
                .variants(request.getVariants().stream()
                        .map(v -> UpdateProductVariantsCommand.VariantCommand.builder()
                                .sku(v.getSku())
                                .price(v.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        updateProductVariantsUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(name = "productId", value = "/{productId}/status")
    public ResponseEntity<Void> updateProductStatus(@PathVariable(name = "productId") UUID productId, @RequestBody UpdateProductStatusRequest request) {
        UpdateProductStatusCommand command = UpdateProductStatusCommand.builder()
                .productId(productId)
                .shopId(request.getShopId())
                .status(request.getStatus())
                .build();

        updateProductStatusUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    // ─── QUERIES ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<SearchProductsProjection> searchProducts(
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
        
        // Map FE page (1-based usually) to Backend offset (0-based) if needed, 
        // assuming FE sends page=1 for the first page. If FE sends 1-based:
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
                
        SearchProductsProjection results = searchProductsUseCase.execute(queryParam);
        return ResponseEntity.ok(results);
    }

    @GetMapping(name = "productId", value = "/{productId}")
    public ResponseEntity<ProductDetailProjection> getProductDetail(@PathVariable(name = "productId") UUID productId){
        GetProductDetailQuery query = new GetProductDetailQuery(productId);
        ProductDetailProjection result = getProductDetailQueryUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top")
    public ResponseEntity<List<ProductDetailProjection>> getTopProducts(
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<ProductDetailProjection> results = getTopProductsUseCase.execute(limit);
        return ResponseEntity.ok(results);
    }
}
