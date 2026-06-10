package com.furnisight.admin.account.role.web.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class UpdateUserRoleRequest {
    private UUID roleId;
    private String action; // e.g. "ASSIGN" or "REVOKE"
}
