package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.collection.dto.command.CreateCollectionCommand;
import com.furnisight.catalog.application.collection.dto.command.UpdateCollectionCommand;
import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;
import com.furnisight.catalog.application.collection.dto.query.GetCollectionDetailQuery;
import com.furnisight.catalog.application.collection.port.in.usecase.*;
import com.furnisight.catalog.presentation.web.rest.dto.request.collection.CreateCollectionRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.collection.UpdateCollectionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CreateCollectionUseCase createCollectionUseCase;
    private final UpdateCollectionUseCase updateCollectionUseCase;
    private final GetCollectionDetailUseCase getCollectionDetailUseCase;
    private final ListCollectionsUseCase listCollectionsUseCase;
    private final DeleteCollectionUseCase deleteCollectionUseCase;

    // ─── COMMANDS ────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Void> createCollection(@RequestBody CreateCollectionRequest request) {
        CreateCollectionCommand command = CreateCollectionCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .build();
        createCollectionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<Void> updateCollection(
            @PathVariable UUID collectionId,
            @RequestBody UpdateCollectionRequest request) {
        UpdateCollectionCommand command = UpdateCollectionCommand.builder()
                .collectionId(collectionId)
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .build();
        updateCollectionUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(@PathVariable UUID collectionId) {
        deleteCollectionUseCase.execute(collectionId);
        return ResponseEntity.noContent().build();
    }

    // ─── QUERIES ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<CollectionDetailProjection>> listCollections() {
        List<CollectionDetailProjection> results = listCollectionsUseCase.execute();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDetailProjection> getCollectionDetail(@PathVariable UUID collectionId) {
        GetCollectionDetailQuery query = new GetCollectionDetailQuery(collectionId);
        CollectionDetailProjection result = getCollectionDetailUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
