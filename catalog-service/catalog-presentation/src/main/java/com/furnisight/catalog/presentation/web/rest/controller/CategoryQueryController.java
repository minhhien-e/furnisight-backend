package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.category.dto.CategoryDetailResponseDto;
import com.furnisight.catalog.application.category.dto.GetCategoryDetailQuery;
import com.furnisight.catalog.application.category.port.in.usecase.GetCategoryDetailQueryUseCase;
import com.furnisight.catalog.application.category.port.in.usecase.ListCategoriesUseCase;
import com.furnisight.catalog.presentation.web.rest.dto.response.category.CategoryDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryQueryController {
    private final GetCategoryDetailQueryUseCase getCategoryDetailQueryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;

    @GetMapping
    public ResponseEntity<List<CategoryDetailResponse>> listCategories() {
        var results = listCategoriesUseCase.execute();
        var response = results.stream()
                .map(CategoryDetailResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDetailResponse> getCategoryDetail(@PathVariable UUID categoryId){
        GetCategoryDetailQuery query = new GetCategoryDetailQuery(categoryId);
        CategoryDetailResponseDto responseDto = getCategoryDetailQueryUseCase.execute(query);
        return ResponseEntity.ok(CategoryDetailResponse.from(responseDto));
    }
}
