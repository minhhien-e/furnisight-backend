package com.furnisight.user.presentation.web.rest.controller.role;

import com.furnisight.user.application.role.dto.command.*;
import com.furnisight.user.application.role.port.in.usecase.*;
import com.furnisight.user.presentation.web.rest.dto.request.role.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
//@PreAuthorize("hasAuthority('MANAGE_ROLES')")
public class AdminRoleController {

    private final AddRoleUseCase addRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final AssignPermissionUseCase assignPermissionUseCase;
    private final RevokeRolePermissionUseCase revokeRolePermissionUseCase;

    @PostMapping
    public ResponseEntity<?> addRole(@RequestBody AddRoleRequest request) {
        var command = new AddRoleCommand(request.name(), request.position());
        addRoleUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<?> updateRole(@PathVariable UUID roleId, @RequestBody UpdateRoleRequest request) {
        var command = new UpdateRoleCommand(roleId, request.name(), request.position());
        updateRoleUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable UUID roleId) {
        var command = new DeleteRoleCommand(roleId);
        deleteRoleUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{roleId}/permissions/assign")
    public ResponseEntity<?> assignPermission(@PathVariable UUID roleId, @RequestBody AssignPermissionRequest request) {
        var command = new AssignPermissionCommand(roleId, request.permission());
        assignPermissionUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{roleId}/permissions/revoke")
    public ResponseEntity<?> revokePermission(@PathVariable UUID roleId, @RequestBody RevokeRolePermissionRequest request) {
        var command = new RevokeRolePermissionCommand(roleId, request.permission());
        revokeRolePermissionUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
}
