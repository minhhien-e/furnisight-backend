package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.in.usecase.AddProductVariantUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.ChangeProductCategoryUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.CreateProductUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.GetProductDetailQueryUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.GetTopProductsUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.GetWeeklyFavoriteProductsUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.RemoveProductVariantUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.SearchProductsUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductInfoUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductStatusUseCase;
import com.furnisight.catalog.application.product.service.ProductTranslationService;
import com.furnisight.catalog.application.translation.port.out.TextTranslationPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Test
    void langQueryParamOverridesAcceptLanguageForSearch() {
        SearchProductsUseCase searchProductsUseCase = mock(SearchProductsUseCase.class);
        ProductController controller = new ProductController(
                mock(CreateProductUseCase.class),
                mock(UpdateProductInfoUseCase.class),
                mock(AddProductVariantUseCase.class),
                mock(RemoveProductVariantUseCase.class),
                mock(UpdateProductStatusUseCase.class),
                mock(GetProductDetailQueryUseCase.class),
                searchProductsUseCase,
                mock(GetTopProductsUseCase.class),
                mock(ChangeProductCategoryUseCase.class),
                mock(GetWeeklyFavoriteProductsUseCase.class),
                new ProductTranslationService(mock(TextTranslationPort.class))
        );
        when(searchProductsUseCase.execute(argThat(query -> "en".equals(query.getLang()))))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0, 24));

        controller.searchProducts("vi-VN,vi;q=0.9", "en", null, null, null, null, null, null, null, null, null, null, 0, 24);

        verify(searchProductsUseCase).execute(argThat(query -> "en".equals(query.getLang())));
    }

    @Test
    void acceptLanguageFallsBackWhenLangQueryIsMissing() {
        GetTopProductsUseCase getTopProductsUseCase = mock(GetTopProductsUseCase.class);
        ProductController controller = new ProductController(
                mock(CreateProductUseCase.class),
                mock(UpdateProductInfoUseCase.class),
                mock(AddProductVariantUseCase.class),
                mock(RemoveProductVariantUseCase.class),
                mock(UpdateProductStatusUseCase.class),
                mock(GetProductDetailQueryUseCase.class),
                mock(SearchProductsUseCase.class),
                getTopProductsUseCase,
                mock(ChangeProductCategoryUseCase.class),
                mock(GetWeeklyFavoriteProductsUseCase.class),
                new ProductTranslationService(mock(TextTranslationPort.class))
        );
        when(getTopProductsUseCase.execute(5, "en")).thenReturn(List.of(ProductResponse.builder().name("Sofa").build()));

        controller.getTopProducts("en-US,en;q=0.9", null, 5);

        verify(getTopProductsUseCase).execute(5, "en");
    }
}
