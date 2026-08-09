package com.furnisight.admin.catalog.roomtype.web;

import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.catalog.roomtype.application.RoomTypeService;
import com.furnisight.admin.catalog.roomtype.web.dto.request.UpsertRoomTypeRequest;
import com.furnisight.admin.catalog.roomtype.web.dto.response.RoomTypeResponse;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<List<RoomTypeResponse>> getRoomTypes(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(roomTypeService.getRoomTypes(query));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> createRoomType(
            @RequestBody UpsertRoomTypeRequest request, HttpServletRequest httpRequest) {
        ActionResultResponse result = roomTypeService.createRoomType(request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.CREATE_CATEGORY,
                request.slug(), result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> updateRoomType(
            @PathVariable String id, @RequestBody UpsertRoomTypeRequest request,
            HttpServletRequest httpRequest) {
        ActionResultResponse result = roomTypeService.updateRoomType(id, request);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.UPDATE_CATEGORY,
                id, result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteRoomType(
            @PathVariable String id, HttpServletRequest httpRequest) {
        ActionResultResponse result = roomTypeService.deleteRoomType(id);
        auditLogService.record(currentUserProvider.getCurrentUserId(), com.furnisight.admin.audit.domain.AuditAction.DELETE_CATEGORY,
                id, result, null, httpRequest);
        return ResponseEntity.ok(result);
    }
}
