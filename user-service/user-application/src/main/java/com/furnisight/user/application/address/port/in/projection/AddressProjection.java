package com.furnisight.user.application.address.port.in.projection;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AddressProjection {
    private UUID id;
    private String fullName;
    private String phone;
    private String provinceCode;
    private String provinceName;
    private String wardCode;
    private String wardName;
    private String detail;
    private String type;
    private boolean isDefault;
}
