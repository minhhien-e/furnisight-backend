package com.furnisight.user.application.address.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.address.port.in.command.AddAddressCommand;
import com.furnisight.user.application.address.port.in.projection.AddressProjection;

public interface AddAddressUseCase extends UseCase<AddAddressCommand, AddressProjection> {
}
