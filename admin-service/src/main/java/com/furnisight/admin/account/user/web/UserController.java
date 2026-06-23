package com.furnisight.admin.account.user.web;

import com.furnisight.admin.account.user.application.UserService;
import com.furnisight.admin.account.user.web.dto.request.CreateUserRequest;
import com.furnisight.admin.account.user.web.dto.request.UpdateUserRequest;
import com.furnisight.admin.account.user.web.dto.request.UpdateUserStatusRequest;
import com.furnisight.admin.account.user.web.dto.response.UserResponse;
import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority(\'ADMIN\')")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String scope) {
        return ResponseEntity.ok(userService.getUsers(page, size, query, status, scope));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\'ADMIN\')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(\'ACCOUNT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> createUser(
            @RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = userService.createUser(adminId, request);
        auditLogService.record(adminId, "create", "Tạo tài khoản admin", "USER",
                request.getEmail(), result, "Email: " + request.getEmail(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\'ACCOUNT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> updateUser(
            @PathVariable UUID id, @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = userService.updateUser(adminId, id, request);
        auditLogService.record(adminId, "update", "Cập nhật tài khoản", "USER",
                id.toString(), result, "User id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority(\'ACCOUNT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> updateUserStatus(
            @PathVariable UUID id, @RequestBody UpdateUserStatusRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = userService.updateUserStatus(adminId, id, request.getStatus());
        auditLogService.record(adminId, "update_status", "Cập nhật trạng thái tài khoản", "USER",
                id.toString(), result, "Trạng thái: " + request.getStatus(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\'ACCOUNT_MANAGE\') or hasAuthority(\'ADMIN\')")
    public ResponseEntity<ActionResultResponse> deleteUser(
            @PathVariable UUID id, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = userService.deleteUser(adminId, id);
        auditLogService.record(adminId, "delete", "Xóa tài khoản", "USER",
                id.toString(), result, "User id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }
}
