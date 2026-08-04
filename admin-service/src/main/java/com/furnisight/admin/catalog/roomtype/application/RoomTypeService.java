package com.furnisight.admin.catalog.roomtype.application;

import com.furnisight.admin.catalog.RoomTypeDto;
import com.furnisight.admin.catalog.CreateRoomTypeRequest;
import com.furnisight.admin.catalog.UpdateRoomTypeRequest;
import com.furnisight.admin.catalog.roomtype.web.dto.request.UpsertRoomTypeRequest;
import com.furnisight.admin.catalog.roomtype.web.dto.response.RoomTypeResponse;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.shared.web.ActionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final AdminCatalogGrpcClient catalogClient;

    public List<RoomTypeResponse> getRoomTypes(String query) {
        com.furnisight.admin.catalog.RoomTypeListResponse response = catalogClient.getRoomTypes(query);
        return response.getRoomTypesList().stream()
                .map(this::toResponse)
                .toList();
    }

    public ActionResultResponse createRoomType(UpsertRoomTypeRequest request) {
        return toActionResult(catalogClient.createRoomType(CreateRoomTypeRequest.newBuilder()
                .setName(value(request.name()))
                .setSlug(value(request.slug()))
                .setDescription(value(request.description()))
                .setVisible(request.visible())
                .setImageUrl(value(request.imageUrl()))
                .setMediaId(value(request.mediaId()))
                .build()));
    }

    public ActionResultResponse updateRoomType(String id, UpsertRoomTypeRequest request) {
        return toActionResult(catalogClient.updateRoomType(UpdateRoomTypeRequest.newBuilder()
                .setId(value(id))
                .setName(value(request.name()))
                .setSlug(value(request.slug()))
                .setDescription(value(request.description()))
                .setVisible(request.visible())
                .setImageUrl(value(request.imageUrl()))
                .setMediaId(value(request.mediaId()))
                .build()));
    }

    public ActionResultResponse deleteRoomType(String id) {
        return toActionResult(catalogClient.deleteRoomType(id));
    }

    private RoomTypeResponse toResponse(RoomTypeDto roomType) {
        return new RoomTypeResponse(
                roomType.getId(), roomType.getName(), roomType.getSlug(),
                roomType.getDescription(),                roomType.getVisible(),
                roomType.getCreatedAt(),
                roomType.getUpdatedAt(),
                roomType.getImageUrl(),
                roomType.getMediaId()
        );}

    private ActionResultResponse toActionResult(com.furnisight.admin.catalog.AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
