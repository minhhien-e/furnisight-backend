package com.furnisight.admin.catalog.product.application;

import com.furnisight.admin.catalog.product.web.dto.request.UpsertProductVariantRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductValidatorTest {

    private final ProductValidator validator = new ProductValidator();

    @Test
    void normalizesSkuAndDefaultsThreshold() {
        assertThat(validator.normalizeSku(" chair-01 ")).isEqualTo("CHAIR-01");
        assertThat(validator.validThreshold(0)).isEqualTo(5);
    }

    @Test
    void rejectsDuplicateSkuIgnoringCase() {
        assertThatThrownBy(() -> validator.validateVariants(List.of(
                variant("chair-01"),
                variant(" CHAIR-01 "))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU variant bị trùng");
    }

    @Test
    void rejectsInvalidThreshold() {
        assertThatThrownBy(() -> validator.validThreshold(10_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 đến 9999");
    }

    private UpsertProductVariantRequest variant(String sku) {
        return new UpsertProductVariantRequest(
                "", sku, 100, 10, "", "", "", 0, 0, 0, 0, 5,
                "", "", false, List.of());
    }
}
