package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.category.dto.command.CreateCategoryCommand;
import com.furnisight.catalog.application.category.dto.command.UpdateCategoryCommand;
import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import com.furnisight.catalog.application.category.dto.query.GetCategoryDetailQuery;
import com.furnisight.catalog.application.category.port.in.usecase.*;
import com.furnisight.catalog.presentation.web.rest.dto.request.category.CreateCategoryRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.category.UpdateCategoryRequest;
import lombok.RequiredArgsConstructor;
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
                .build();
        updateCategoryUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    // ─── QUERIES ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<CategoryDetailProjection>> listCategories() {
        List<CategoryDetailProjection> results = listCategoriesUseCase.execute();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryDetailProjection>> listRootCategories() {
        List<CategoryDetailProjection> results = listRootCategoriesUseCase.execute();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{slug}/subcategories")
    public ResponseEntity<List<CategoryDetailProjection>> listSubcategories(@PathVariable String slug) {
        List<CategoryDetailProjection> results = listSubcategoriesUseCase.execute(slug);
        return ResponseEntity.ok(results);
    }

    @GetMapping(name = "slug", value = "/{slug}")
    public ResponseEntity<CategoryDetailProjection> getCategoryDetail(@PathVariable String slug){
        GetCategoryDetailQuery query = new GetCategoryDetailQuery(slug);
        CategoryDetailProjection result = getCategoryDetailUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
