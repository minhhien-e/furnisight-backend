package com.furnisight.user.presentation.web.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.furnisight.user.application.address.port.in.projection.AddressProjection;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AddressResponse {
    private UUID id;
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

    @JsonProperty("isDefault")
    private boolean defaultAddress;

    public static AddressResponse from(AddressProjection address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .provinceCode(address.getProvinceCode())
                .provinceName(address.getProvinceName())
                .districtCode(address.getDistrictCode())
                .districtName(address.getDistrictName())
                .wardCode(address.getWardCode())
                .wardName(address.getWardName())
                .detail(address.getDetail())
                .type(address.getType())
                .defaultAddress(address.isDefault())
                .build();
    }
}
