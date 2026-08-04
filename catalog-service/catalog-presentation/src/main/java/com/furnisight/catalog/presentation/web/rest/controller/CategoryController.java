package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.category.dto.command.CreateCategoryCommand;
import com.furnisight.catalog.application.category.dto.command.UpdateCategoryCommand;
import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.dto.query.GetCategoryDetailQuery;
import com.furnisight.catalog.application.product.service.ProductTranslationService;
import com.furnisight.catalog.application.category.port.in.usecase.*;
import com.furnisight.catalog.presentation.web.rest.dto.request.category.CreateCategoryRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.category.UpdateCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final GetCategoryDetailUseCase getCategoryDetailUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final ListRootCategoriesUseCase listRootCategoriesUseCase;
    private final ListSubcategoriesUseCase listSubcategoriesUseCase;
    private final ProductTranslationService productTranslationService;

    // ─── COMMANDS ────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Void> createCategory(@RequestBody CreateCategoryRequest request){
        CreateCategoryCommand command = CreateCategoryCommand.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .parentId(request.getParentId())
                .iconId(request.getIconId())
                .visible(request.getVisible())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .roomTypeId(request.getRoomTypeId())
                .build();
        createCategoryUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(name = "categoryId", value = "/{categoryId}")
    public ResponseEntity<Void> updateCategory(@PathVariable UUID categoryId, @RequestBody UpdateCategoryRequest request){
        UpdateCategoryCommand command = UpdateCategoryCommand.builder()
                .categoryId(categoryId)
                .name(request.getName())
                .slug(request.getSlug())
                .parentId(request.getParentId())
                .iconId(request.getIconId())
                .visible(request.getVisible())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .roomTypeId(request.getRoomTypeId())
                .build();
        updateCategoryUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    // ─── QUERIES ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        String resolvedLang = resolveLang(lang, acceptLanguage);
        List<CategoryResponse> results = listCategoriesUseCase.execute(resolvedLang);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> listRootCategories(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        String resolvedLang = resolveLang(lang, acceptLanguage);
        List<CategoryResponse> results = listRootCategoriesUseCase.execute(resolvedLang);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{slug}/subcategories")
    public ResponseEntity<List<CategoryResponse>> listSubcategories(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @PathVariable String slug) {
        String resolvedLang = resolveLang(lang, acceptLanguage);
        List<CategoryResponse> results = listSubcategoriesUseCase.execute(slug, resolvedLang);
        return ResponseEntity.ok(results);
    }

    @GetMapping(name = "slug", value = "/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryDetail(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @PathVariable String slug){
        String resolvedLang = resolveLang(lang, acceptLanguage);
        GetCategoryDetailQuery query = new GetCategoryDetailQuery(slug, resolvedLang);
        CategoryResponse result = getCategoryDetailUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    private String resolveLang(String langParam, String acceptLanguage) {
        if (langParam != null && !langParam.isBlank()) {
            return productTranslationService.normalizeLang(langParam);
        }
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            return productTranslationService.normalizeLang(acceptLanguage);
        }
        return ProductTranslationService.SOURCE_LANG_VI;
    }
}
