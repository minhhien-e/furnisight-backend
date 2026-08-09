package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.roomtype.dto.response.RoomTypeResponse;
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
    private static final java.util.concurrent.ExecutorService EXECUTOR = java.util.concurrent.Executors.newCachedThreadPool();

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
        // User typed query in English, translate it to Vietnamese to search in DB
        return textTranslationPort.translate(query, TARGET_LANG_EN, SOURCE_LANG_VI);
    }

    public ProductResponse localizeProduct(ProductResponse product, String targetLang) {
        if (product == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return product;
        }

        java.util.List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();

        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> product.setName(translateValue(product.getName())), EXECUTOR));
        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> product.setDescription(translateValue(product.getDescription())), EXECUTOR));
        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> product.setFeatures(translateList(product.getFeatures())), EXECUTOR));
        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> product.setCategoryName(translateValue(product.getCategoryName())), EXECUTOR));

        if (product.getCategory() != null) {
            futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> product.getCategory().setLabel(translateValue(product.getCategory().getLabel())), EXECUTOR));
            futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> product.getCategory().setParentLabel(translateValue(product.getCategory().getParentLabel())), EXECUTOR));
        }

        if (product.getVariants() != null) {
            for (var variant : product.getVariants()) {
                futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> variant.setMaterial(translateValue(variant.getMaterial())), EXECUTOR));
                futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> variant.setColor(translateValue(variant.getColor())), EXECUTOR));
                futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> variant.setWarranty(translateValue(variant.getWarranty())), EXECUTOR));
                futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> variant.setFeatures(translateList(variant.getFeatures())), EXECUTOR));
                if (variant.getSpecifications() != null) {
                    for (var entry : variant.getSpecifications().entrySet()) {
                        if (entry.getValue() instanceof String str) {
                            futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> entry.setValue(translateValue(str)), EXECUTOR));
                        }
                    }
                }
            }
        }
        
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
        return product;
    }

    public List<ProductResponse> localizeProducts(List<ProductResponse> products, String targetLang) {
        if (products == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return products;
        }
        java.util.List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        for (ProductResponse product : products) {
            futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> localizeProduct(product, targetLang), EXECUTOR));
        }
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
        return products;
    }

    public PageResponse<ProductResponse> localizePage(PageResponse<ProductResponse> page, String targetLang) {
        if (page == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return page;
        }

        List<ProductResponse> items = page.items();
        localizeProducts(items, targetLang);
        return new PageResponse<>(items, page.totalPages(), page.totalElements(), page.currentPage(), page.pageSize());
    }

    public CategoryResponse localizeCategory(CategoryResponse category, String targetLang) {
        if (category == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return category;
        }

        java.util.List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> category.setName(translateValue(category.getName())), EXECUTOR));
        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> category.setDescription(translateValue(category.getDescription())), EXECUTOR));
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
        
        return category;
    }

    public List<CategoryResponse> localizeCategories(List<CategoryResponse> categories, String targetLang) {
        if (categories == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return categories;
        }
        java.util.List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        for (CategoryResponse category : categories) {
            futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> localizeCategory(category, targetLang), EXECUTOR));
        }
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
        return categories;
    }

    public RoomTypeResponse localizeRoomType(RoomTypeResponse roomType, String targetLang) {
        if (roomType == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return roomType;
        }

        java.util.List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> roomType.setName(translateValue(roomType.getName())), EXECUTOR));
        futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> roomType.setDescription(translateValue(roomType.getDescription())), EXECUTOR));
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();

        return roomType;
    }

    public List<RoomTypeResponse> localizeRoomTypes(List<RoomTypeResponse> roomTypes, String targetLang) {
        if (roomTypes == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return roomTypes;
        }
        java.util.List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        for (RoomTypeResponse roomType : roomTypes) {
            futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> localizeRoomType(roomType, targetLang), EXECUTOR));
        }
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
        return roomTypes;
    }

    private String translateValue(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        // DB is VI, output is EN
        return textTranslationPort.translate(value, SOURCE_LANG_VI, TARGET_LANG_EN);
    }

    private List<String> translateList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return values;
        }
        java.util.List<java.util.concurrent.CompletableFuture<String>> futures = new java.util.ArrayList<>();
        for (String val : values) {
            futures.add(java.util.concurrent.CompletableFuture.supplyAsync(() -> translateValue(val), EXECUTOR));
        }
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
        return futures.stream().map(java.util.concurrent.CompletableFuture::join).toList();
    }
}
