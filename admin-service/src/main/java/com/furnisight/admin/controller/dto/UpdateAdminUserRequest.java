package com.furnisight.admin.controller.dto;

import lombok.Data;

@Data
public class UpdateAdminUserRequest {
    private String name;
    private String email;
    private String phone;
    private String role;
    private String roleId;
    private String status;
}
