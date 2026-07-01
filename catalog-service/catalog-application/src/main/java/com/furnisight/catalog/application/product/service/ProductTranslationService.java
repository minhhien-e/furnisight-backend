package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.translation.port.out.TextTranslationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductTranslationService {

    public static final String SOURCE_LANG_VI = "vi";
    public static final String TARGET_LANG_EN = "en";

    private final TextTranslationPort textTranslationPort;

    public String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return SOURCE_LANG_VI;
        }

        String normalized = lang.trim().toLowerCase();
        if (normalized.startsWith(TARGET_LANG_EN)) {
            return TARGET_LANG_EN;
        }
        return SOURCE_LANG_VI;
    }

    public String translateSearchQuery(String query, String targetLang) {
        if (!TARGET_LANG_EN.equals(normalizeLang(targetLang)) || query == null || query.isBlank()) {
            return query;
        }
        return textTranslationPort.translate(query, TARGET_LANG_EN, SOURCE_LANG_VI);
    }

    public ProductResponse localizeProduct(ProductResponse product, String targetLang) {
        if (product == null || !TARGET_LANG_EN.equals(normalizeLang(targetLang))) {
            return product;
        }

        product.setName(translateValue(product.getName()));
        product.setDescription(translateValue(product.getDescription()));
        product.setFeatures(translateList(product.getFeatures()));
        product.setCategoryName(translateValue(product.getCategoryName()));

        if (product.getCategory() != null) {
            product.getCategory().setLabel(translateValue(product.getCategory().getLabel()));
        }

        if (product.getVariants() != null) {
            product.getVariants().forEach(variant -> {
                variant.setMaterial(translateValue(variant.getMaterial()));
                variant.setColor(translateValue(variant.getColor()));
                variant.setWarranty(translateValue(variant.getWarranty()));
            });
        }

        return product;
    }

    public List<ProductResponse> localizeProducts(List<ProductResponse> products, String targetLang) {
        if (products == null || !TARGET_LANG_EN.equals(normalizeLang(targetLang))) {
            return products;
        }
        products.forEach(product -> localizeProduct(product, targetLang));
        return products;
    }

    public PageResponse<ProductResponse> localizePage(PageResponse<ProductResponse> page, String targetLang) {
        if (page == null || !TARGET_LANG_EN.equals(normalizeLang(targetLang))) {
            return page;
        }

        List<ProductResponse> items = page.items();
        localizeProducts(items, targetLang);
        return new PageResponse<>(items, page.totalPages(), page.totalElements(), page.currentPage(), page.pageSize());
    }

    public CategoryResponse localizeCategory(CategoryResponse category, String targetLang) {
        if (category == null || !TARGET_LANG_EN.equals(normalizeLang(targetLang))) {
            return category;
        }

        category.setName(translateValue(category.getName()));
        category.setDescription(translateValue(category.getDescription()));
        return category;
    }

    public List<CategoryResponse> localizeCategories(List<CategoryResponse> categories, String targetLang) {
        if (categories == null || !TARGET_LANG_EN.equals(normalizeLang(targetLang))) {
            return categories;
        }
        categories.forEach(category -> localizeCategory(category, targetLang));
        return categories;
    }

    private String translateValue(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return textTranslationPort.translate(value, SOURCE_LANG_VI, TARGET_LANG_EN);
    }

    private List<String> translateList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return values;
        }
        return values.stream().map(this::translateValue).toList();
    }
}
