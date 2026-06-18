package com.furnisight.admin.catalog.category.application;

import com.furnisight.admin.catalog.CategoryDto;
import com.furnisight.admin.catalog.CreateCategoryRequest;
import com.furnisight.admin.catalog.UpdateCategoryRequest;
import com.furnisight.admin.catalog.category.web.dto.request.UpsertCategoryRequest;
import com.furnisight.admin.catalog.category.web.dto.response.CategoryResponse;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.shared.web.ActionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final AdminCatalogGrpcClient catalogClient;

    public List<CategoryResponse> getCategories(String query) {
        com.furnisight.admin.catalog.CategoryListResponse response = catalogClient.getCategories(query);
        return response.getCategoriesList().stream()
                .map(this::toResponse)
                .toList();
    }

    public ActionResultResponse createCategory(UpsertCategoryRequest request) {
        return toActionResult(catalogClient.createCategory(CreateCategoryRequest.newBuilder()
                .setName(value(request.name()))
                .setSlug(value(request.slug()))
                .setIconId(value(request.iconId()))
                .setVisible(request.visible())
                .setDescription(value(request.description()))
                .setImageUrl(value(request.imageUrl()))
                .build()));
    }

    public ActionResultResponse updateCategory(String id, UpsertCategoryRequest request) {
        return toActionResult(catalogClient.updateCategory(UpdateCategoryRequest.newBuilder()
                .setId(value(id))
                .setName(value(request.name()))
                .setSlug(value(request.slug()))
                .setIconId(value(request.iconId()))
                .setVisible(request.visible())
                .setDescription(value(request.description()))
                .setImageUrl(value(request.imageUrl()))
                .build()));
    }

    public ActionResultResponse deleteCategory(String id) {
        return toActionResult(catalogClient.deleteCategory(id));
    }

    private CategoryResponse toResponse(CategoryDto category) {
        return new CategoryResponse(
                category.getId(), category.getName(), category.getSlug(), category.getProductCount(),
                category.getVisible(), category.getVisibleLabel(), category.getCreatedAt(),
                category.getIconId(), category.getDescription(), category.getImageUrl());
    }

    private ActionResultResponse toActionResult(com.furnisight.admin.catalog.AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
