package com.furnisight.user.application.role.dto.command;

import java.util.UUID;

public record AssignPermissionCommand(UUID roleId, String permission) {
}
