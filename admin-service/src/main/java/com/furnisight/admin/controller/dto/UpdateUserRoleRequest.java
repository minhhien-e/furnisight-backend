package com.furnisight.admin.controller.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UpdateUserRoleRequest {
    private UUID roleId;
    private String action; // e.g. "ASSIGN" or "REVOKE"
}
