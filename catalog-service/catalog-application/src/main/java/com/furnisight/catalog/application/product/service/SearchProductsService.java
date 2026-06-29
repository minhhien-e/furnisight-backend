package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.in.usecase.SearchProductsUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

@Service
@RequiredArgsConstructor
public class SearchProductsService implements SearchProductsUseCase {

    private final ProductReadRepository productReadRepository;
    private final ProductTranslationService productTranslationService;

    @Override
    public PageResponse<ProductResponse> execute(SearchProductsQuery query) {
        SearchProductsQuery effectiveQuery = SearchProductsQuery.builder()
                .lang(productTranslationService.normalizeLang(query.getLang()))
                .q(productTranslationService.translateSearchQuery(query.getQ(), query.getLang()))
                .category(query.getCategory())
                .sort(query.getSort())
                .priceBands(query.getPriceBands())
                .priceSliderPct(query.getPriceSliderPct())
                .materials(query.getMaterials())
                .colors(query.getColors())
                .minStar(query.getMinStar())
                .saleOnly(query.getSaleOnly())
                .status(query.getStatus())
                .page(query.getPage())
                .size(query.getSize())
                .build();

        return productTranslationService.localizePage(
                productReadRepository.searchProducts(effectiveQuery),
                effectiveQuery.getLang());
    }
}
