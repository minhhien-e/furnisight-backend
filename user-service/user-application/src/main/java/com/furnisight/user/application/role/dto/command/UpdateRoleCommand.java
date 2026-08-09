package com.furnisight.user.application.role.dto.command;

import java.util.UUID;

public record UpdateRoleCommand(UUID roleId, String newName, int newPosition) {
}
