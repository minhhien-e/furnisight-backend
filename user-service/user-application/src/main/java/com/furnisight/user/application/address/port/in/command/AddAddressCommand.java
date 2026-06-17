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
    private String wardCode;
    private String wardName;
    private String detail;
    private String type;
    @com.fasterxml.jackson.annotation.JsonProperty("isDefault")
    private boolean isDefault;
}
