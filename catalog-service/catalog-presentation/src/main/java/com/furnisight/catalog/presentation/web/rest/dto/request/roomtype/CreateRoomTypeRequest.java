package com.furnisight.catalog.presentation.web.rest.dto.request.roomtype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomTypeRequest {
    private String name;
    private String slug;
    private String description;
}
