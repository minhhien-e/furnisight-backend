package com.furnisight.user.application.address.service;

import com.furnisight.user.application.address.port.in.response.AddressResponse;
import com.furnisight.user.application.address.port.in.usecase.GetAddressesUseCase;
import com.furnisight.user.domain.entities.profile.UserAddress;
import com.furnisight.user.domain.repository.profile.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAddressesService implements GetAddressesUseCase {

    private final UserAddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> execute(UUID accountId) {
        return addressRepository.findByAccountId(accountId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AddressResponse mapToResponse(UserAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .provinceCode(address.getProvinceCode())
                .provinceName(address.getProvinceName())
                .wardCode(address.getWardCode())
                .wardName(address.getWardName())
                .detail(address.getDetail())
                .type(address.getType())
                .isDefault(address.isDefault())
                .build();
    }
}
