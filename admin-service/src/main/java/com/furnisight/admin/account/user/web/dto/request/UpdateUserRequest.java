package com.furnisight.admin.account.user.web.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String email;
    private String phone;
    private String role;
    private String roleId;
    private String status;
}
