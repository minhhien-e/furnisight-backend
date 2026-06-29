package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.application.translation.port.out.TextTranslationPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchProductsServiceTest {

    @Test
    void enSearchTranslatesQueryBeforeRepositoryAndLocalizesResponse() {
        ProductReadRepository repository = mock(ProductReadRepository.class);
        TextTranslationPort translator = mock(TextTranslationPort.class);
        ProductTranslationService translationService = new ProductTranslationService(translator);
        SearchProductsService service = new SearchProductsService(repository, translationService);

        when(translator.translate("walnut sofa", "en", "vi")).thenReturn("ghế sofa gỗ óc chó");
        when(translator.translate("Ghế sofa gỗ óc chó", "vi", "en")).thenReturn("Walnut wood sofa");

        when(repository.searchProducts(argThat(query ->
                "ghế sofa gỗ óc chó".equals(query.getQ()) && "en".equals(query.getLang()))))
                .thenReturn(new PageResponse<>(
                        List.of(ProductResponse.builder()
                                .id(UUID.randomUUID())
                                .name("Ghế sofa gỗ óc chó")
                                .build()),
                        1,
                        1,
                        0,
                        24));

        PageResponse<ProductResponse> result = service.execute(SearchProductsQuery.builder()
                .lang("en")
                .q("walnut sofa")
                .page(0)
                .size(24)
                .build());

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getName()).isEqualTo("Walnut wood sofa");
        verify(repository).searchProducts(argThat(query -> "ghế sofa gỗ óc chó".equals(query.getQ())));
    }
}
