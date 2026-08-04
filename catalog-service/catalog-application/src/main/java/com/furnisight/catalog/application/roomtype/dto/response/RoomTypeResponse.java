package com.furnisight.catalog.application.roomtype.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeResponse implements Serializable {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private UUID mediaId;
    private Boolean visible;
    private Integer productCount;
}
