package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.roomtype.dto.response.RoomTypeResponse;
import com.furnisight.catalog.application.translation.port.out.TextTranslationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        // User typed query in English, translate it to Vietnamese to search in DB
        return textTranslationPort.translate(query, TARGET_LANG_EN, SOURCE_LANG_VI);
    }

    private class TranslationBatcher {
        private final List<String> texts = new ArrayList<>();
        private final List<Consumer<String>> setters = new ArrayList<>();

        public void add(String text, Consumer<String> setter) {
            if (text != null && !text.isBlank()) {
                texts.add(text);
                setters.add(setter);
            }
        }

        public void addList(List<String> list, Consumer<List<String>> listSetter) {
            if (list == null || list.isEmpty()) return;
            
            // To translate a list, we collect each item and then construct a new list.
            // But since the items are translated in batch, we can use an array/list to collect the results
            // and set the final list once all items are translated.
            final String[] translatedArr = new String[list.size()];
            final int[] completedCount = {0};
            
            for (int i = 0; i < list.size(); i++) {
                final int index = i;
                String item = list.get(i);
                add(item, translatedItem -> {
                    translatedArr[index] = translatedItem;
                    completedCount[0]++;
                    if (completedCount[0] == list.size()) {
                        List<String> translatedList = new ArrayList<>();
                        for (String str : translatedArr) {
                            if (str != null) translatedList.add(str);
                        }
                        listSetter.accept(translatedList);
                    }
                });
            }
        }

        public void execute(String source, String target) {
            if (texts.isEmpty()) return;
            List<String> translated = textTranslationPort.translateBatch(texts, source, target);
            if (translated != null && translated.size() == texts.size()) {
                for (int i = 0; i < translated.size(); i++) {
                    setters.get(i).accept(translated.get(i));
                }
            }
        }
    }

    private void collectProduct(ProductResponse product, TranslationBatcher batcher, boolean isSummary) {
        if (product == null) return;
        
        batcher.add(product.getName(), product::setName);
        batcher.add(product.getCategoryName(), product::setCategoryName);

        if (product.getCategory() != null) {
            batcher.add(product.getCategory().getLabel(), product.getCategory()::setLabel);
            batcher.add(product.getCategory().getParentLabel(), product.getCategory()::setParentLabel);
        }

        if (!isSummary) {
            batcher.add(product.getDescription(), product::setDescription);
            batcher.addList(product.getFeatures(), product::setFeatures);

            if (product.getVariants() != null) {
                for (var variant : product.getVariants()) {
                    batcher.add(variant.getMaterial(), variant::setMaterial);
                    batcher.add(variant.getColor(), variant::setColor);
                    batcher.add(variant.getWarranty(), variant::setWarranty);
                    batcher.addList(variant.getFeatures(), variant::setFeatures);
                    if (variant.getSpecifications() != null) {
                        for (var entry : variant.getSpecifications().entrySet()) {
                            if (entry.getValue() instanceof String str) {
                                batcher.add(str, translatedStr -> entry.setValue(translatedStr));
                            }
                        }
                    }
                }
            }
        }
    }

    private void collectCategory(CategoryResponse category, TranslationBatcher batcher) {
        if (category == null) return;
        batcher.add(category.getName(), category::setName);
        batcher.add(category.getDescription(), category::setDescription);
    }

    private void collectRoomType(RoomTypeResponse roomType, TranslationBatcher batcher) {
        if (roomType == null) return;
        batcher.add(roomType.getName(), roomType::setName);
        batcher.add(roomType.getDescription(), roomType::setDescription);
    }

    public ProductResponse localizeProduct(ProductResponse product, String targetLang) {
        if (product == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return product;
        }

        TranslationBatcher batcher = new TranslationBatcher();
        collectProduct(product, batcher, false);
        batcher.execute(SOURCE_LANG_VI, TARGET_LANG_EN);
        
        return product;
    }

    public List<ProductResponse> localizeProducts(List<ProductResponse> products, String targetLang) {
        if (products == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return products;
        }
        
        TranslationBatcher batcher = new TranslationBatcher();
        for (ProductResponse product : products) {
            collectProduct(product, batcher, true);
        }
        batcher.execute(SOURCE_LANG_VI, TARGET_LANG_EN);
        
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

        TranslationBatcher batcher = new TranslationBatcher();
        collectCategory(category, batcher);
        batcher.execute(SOURCE_LANG_VI, TARGET_LANG_EN);
        
        return category;
    }

    public List<CategoryResponse> localizeCategories(List<CategoryResponse> categories, String targetLang) {
        if (categories == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return categories;
        }
        
        TranslationBatcher batcher = new TranslationBatcher();
        for (CategoryResponse category : categories) {
            collectCategory(category, batcher);
        }
        batcher.execute(SOURCE_LANG_VI, TARGET_LANG_EN);
        
        return categories;
    }

    public RoomTypeResponse localizeRoomType(RoomTypeResponse roomType, String targetLang) {
        if (roomType == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return roomType;
        }

        TranslationBatcher batcher = new TranslationBatcher();
        collectRoomType(roomType, batcher);
        batcher.execute(SOURCE_LANG_VI, TARGET_LANG_EN);

        return roomType;
    }

    public List<RoomTypeResponse> localizeRoomTypes(List<RoomTypeResponse> roomTypes, String targetLang) {
        if (roomTypes == null || SOURCE_LANG_VI.equals(normalizeLang(targetLang))) {
            return roomTypes;
        }
        
        TranslationBatcher batcher = new TranslationBatcher();
        for (RoomTypeResponse roomType : roomTypes) {
            collectRoomType(roomType, batcher);
        }
        batcher.execute(SOURCE_LANG_VI, TARGET_LANG_EN);
        
        return roomTypes;
    }
}
