package com.furnisight.user.application.address.port.in.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressCommand {
    private UUID accountId;
    private UUID addressId;
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
