package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.roomtype.dto.response.RoomTypeResponse;
import com.furnisight.catalog.domain.entities.RoomType;
import com.furnisight.catalog.domain.repository.RoomTypeRepository;
import com.furnisight.catalog.domain.repository.ProductRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.furnisight.catalog.application.product.service.ProductTranslationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeRepository roomTypeRepository;
    private final ProductRepository productRepository;
    private final ProductTranslationService productTranslationService;

    @GetMapping
    public ResponseEntity<List<RoomTypeResponse>> listRoomTypes(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        String resolvedLang = resolveLang(lang, acceptLanguage);
        List<RoomTypeResponse> responses = roomTypeRepository.findByVisibleTrue().stream()
                .map(entity -> productTranslationService.localizeRoomType(mapToResponse(entity), resolvedLang))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<RoomTypeResponse> getRoomType(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @PathVariable String idOrSlug) {
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
        
        String resolvedLang = resolveLang(lang, acceptLanguage);
        return ResponseEntity.ok(productTranslationService.localizeRoomType(mapToResponse(roomType), resolvedLang));
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
    
    private String resolveLang(String langParam, String acceptLanguage) {
        if (langParam != null && !langParam.isBlank()) {
            return productTranslationService.normalizeLang(langParam);
        }
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            return productTranslationService.normalizeLang(acceptLanguage);
        }
        return com.furnisight.catalog.application.product.service.ProductTranslationService.SOURCE_LANG_VI;
    }
}
