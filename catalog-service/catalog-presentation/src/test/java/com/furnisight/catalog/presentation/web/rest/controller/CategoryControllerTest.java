package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.port.in.usecase.CreateCategoryUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.GetCategoryDetailUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.ListCategoriesUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.ListRootCategoriesUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.ListSubcategoriesUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.UpdateCategoryUseCase;
import com.furnisight.catalog.application.product.service.ProductTranslationService;
import com.furnisight.catalog.application.translation.port.out.TextTranslationPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryControllerTest {

    @Test
    void langQueryParamOverridesAcceptLanguageForCategories() {
        ListRootCategoriesUseCase listRootCategoriesUseCase = mock(ListRootCategoriesUseCase.class);
        TextTranslationPort translationPort = mock(TextTranslationPort.class);
        ProductTranslationService translationService = new ProductTranslationService(translationPort);
        CategoryController controller = new CategoryController(
                mock(CreateCategoryUseCase.class),
                mock(UpdateCategoryUseCase.class),
                mock(GetCategoryDetailUseCase.class),
                mock(ListCategoriesUseCase.class),
                listRootCategoriesUseCase,
                mock(ListSubcategoriesUseCase.class),
                translationService
        );

        when(listRootCategoriesUseCase.execute()).thenReturn(List.of(
                CategoryResponse.builder().name("Phòng ngủ").description("Đồ nội thất").build()
        ));
        when(translationPort.translate("Phòng ngủ", "vi", "en")).thenReturn("Bedroom");
        when(translationPort.translate("Đồ nội thất", "vi", "en")).thenReturn("Furniture");

        List<CategoryResponse> response = controller
                .listRootCategories("vi-VN,vi;q=0.9", "en")
                .getBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getName()).isEqualTo("Bedroom");
        assertThat(response.get(0).getDescription()).isEqualTo("Furniture");
    }
}
