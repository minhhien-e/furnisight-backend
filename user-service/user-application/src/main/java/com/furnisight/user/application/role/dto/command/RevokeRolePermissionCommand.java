package com.furnisight.user.application.role.dto.command;

import java.util.UUID;

public record RevokeRolePermissionCommand(UUID roleId, String permission) {
}
