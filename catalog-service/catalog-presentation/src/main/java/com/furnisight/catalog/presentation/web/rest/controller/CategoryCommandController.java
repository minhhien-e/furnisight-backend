package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.category.dto.CreateCategoryCommand;
import com.furnisight.catalog.application.category.dto.UpdateCategoryCommand;
import com.furnisight.catalog.application.category.port.in.usecase.CreateCategoryUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.UpdateCategoryUseCase;
import com.furnisight.catalog.presentation.web.rest.dto.request.category.CreateCategoryRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.category.UpdateCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryCommandController {
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;

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

    @PutMapping("{categoryId}")
    public ResponseEntity<Void> updateCategory(@PathVariable UUID categoryId, @RequestBody UpdateCategoryRequest request){
        UpdateCategoryCommand command = UpdateCategoryCommand.builder()
                .categoryId(categoryId)
                .name(request.getName())
                .slug(request.getSlug())
                .parentId(request.getParentId())
                .build();
        updateCategoryUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
}
