package com.furnisight.user.application.address.port.in.command;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAddressCommand {
    private UUID accountId;
    private String fullName;
    private String phone;
    private String provinceCode;
    private String provinceName;
    private String districtCode;
    private String districtName;
    private String wardCode;
    private String wardName;
    private String detail;
    private String type;
    private boolean isDefault;
}
