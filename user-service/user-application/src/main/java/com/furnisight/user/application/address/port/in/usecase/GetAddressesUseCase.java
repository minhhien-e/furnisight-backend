package com.furnisight.user.application.address.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.address.port.in.projection.AddressProjection;

import java.util.List;
import java.util.UUID;

public interface GetAddressesUseCase extends UseCase<UUID, List<AddressProjection>> {
}
