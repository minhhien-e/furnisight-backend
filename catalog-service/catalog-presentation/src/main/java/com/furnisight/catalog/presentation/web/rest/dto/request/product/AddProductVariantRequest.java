package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductVariantRequest {
    private Double price;
    private Integer stockQuantity;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
}
