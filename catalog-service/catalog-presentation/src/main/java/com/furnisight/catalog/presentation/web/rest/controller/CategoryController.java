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

    // ─── COMMANDS ────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Void> createCategory(@RequestBody CreateCategoryRequest request){
        CreateCategoryCommand command = CreateCategoryCommand.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .parentId(request.getParentId())
                .build();
        createCategoryUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(name = "categoryId", value = "/{categoryId}")
    public ResponseEntity<Void> updateCategory(@PathVariable(name = "categoryId") UUID categoryId, @RequestBody UpdateCategoryRequest request){
        UpdateCategoryCommand command = UpdateCategoryCommand.builder()
                .categoryId(categoryId)
                .name(request.getName())
                .slug(request.getSlug())
                .parentId(request.getParentId())
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

    @GetMapping(name = "categoryId", value = "/{categoryId}")
    public ResponseEntity<CategoryDetailProjection> getCategoryDetail(@PathVariable(name = "categoryId") UUID categoryId){
        GetCategoryDetailQuery query = new GetCategoryDetailQuery(categoryId);
        CategoryDetailProjection result = getCategoryDetailUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
