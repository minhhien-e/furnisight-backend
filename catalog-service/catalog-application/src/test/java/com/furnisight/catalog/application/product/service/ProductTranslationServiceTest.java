package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.translation.port.out.TextTranslationPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductTranslationServiceTest {

    @Test
    void viLocaleKeepsOriginalValuesWithoutCallingTranslator() {
        TextTranslationPort port = mock(TextTranslationPort.class);
        ProductTranslationService service = new ProductTranslationService(port);
        ProductResponse product = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Ghế sofa")
                .description("Sofa gỗ")
                .build();

        ProductResponse localized = service.localizeProduct(product, "vi");

        assertThat(localized.getName()).isEqualTo("Ghế sofa");
        assertThat(localized.getDescription()).isEqualTo("Sofa gỗ");
        verify(port, never()).translate(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void enLocaleTranslatesSupportedProductFields() {
        TextTranslationPort port = mock(TextTranslationPort.class);
        when(port.translate("Ghế sofa", "vi", "en")).thenReturn("Sofa");
        when(port.translate("Mô tả", "vi", "en")).thenReturn("Description");
        when(port.translate("tag-moi", "vi", "en")).thenReturn("new-tag");
        when(port.translate("Tính năng", "vi", "en")).thenReturn("Feature");
        when(port.translate("Phòng ngủ", "vi", "en")).thenReturn("Bedroom");
        when(port.translate("Gỗ", "vi", "en")).thenReturn("Wood");
        when(port.translate("Nâu", "vi", "en")).thenReturn("Brown");
        when(port.translate("12 tháng", "vi", "en")).thenReturn("12 months");

        ProductResponse product = ProductResponse.builder()
                .name("Ghế sofa")
                .description("Mô tả")
                .tags(List.of("tag-moi"))
                .features(List.of("Tính năng"))
                .categoryName("Phòng ngủ")
                .category(ProductResponse.CategoryInfo.builder().label("Phòng ngủ").build())
                .variants(List.of(ProductResponse.VariantDto.builder()
                        .material("Gỗ")
                        .color("Nâu")
                        .warranty("12 tháng")
                        .build()))
                .build();

        ProductResponse localized = new ProductTranslationService(port).localizeProduct(product, "en");

        assertThat(localized.getName()).isEqualTo("Sofa");
        assertThat(localized.getDescription()).isEqualTo("Description");
        assertThat(localized.getTags()).containsExactly("new-tag");
        assertThat(localized.getFeatures()).containsExactly("Feature");
        assertThat(localized.getCategoryName()).isEqualTo("Bedroom");
        assertThat(localized.getCategory().getLabel()).isEqualTo("Bedroom");
        assertThat(localized.getVariants().get(0).getMaterial()).isEqualTo("Wood");
        assertThat(localized.getVariants().get(0).getColor()).isEqualTo("Brown");
        assertThat(localized.getVariants().get(0).getWarranty()).isEqualTo("12 months");
    }
}
