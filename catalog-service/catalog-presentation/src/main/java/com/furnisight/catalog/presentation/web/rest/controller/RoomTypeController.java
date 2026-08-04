package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.roomtype.dto.response.RoomTypeResponse;
import com.furnisight.catalog.domain.entities.RoomType;
import com.furnisight.catalog.domain.repository.RoomTypeRepository;
import com.furnisight.catalog.domain.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeRepository roomTypeRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<RoomTypeResponse>> listRoomTypes() {
        List<RoomTypeResponse> responses = roomTypeRepository.findByVisibleTrue().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<RoomTypeResponse> getRoomType(@PathVariable String idOrSlug) {
        RoomType roomType = null;
        try {
            UUID id = UUID.fromString(idOrSlug);
            roomType = roomTypeRepository.findById(id).orElse(null);
        } catch (IllegalArgumentException ignored) {
            roomType = roomTypeRepository.findBySlug(idOrSlug).orElse(null);
        }

        if (roomType == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapToResponse(roomType));
    }

    private RoomTypeResponse mapToResponse(RoomType entity) {
        return RoomTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .mediaId(entity.getMediaId())
                .visible(entity.getVisible())
                .productCount(productRepository.countByRoomTypeId(entity.getId()))
                .build();
    }
}
