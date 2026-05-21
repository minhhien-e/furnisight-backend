package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import com.furnisight.catalog.domain.enums.ProductStatus;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductStatusRequest {
    private ProductStatus status;
}
