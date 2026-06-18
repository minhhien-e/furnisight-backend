package com.furnisight.user.application.address.port.in.usecase;

import com.furnisight.user.application.address.port.in.command.UpdateAddressCommand;
import com.furnisight.user.application.address.port.in.response.AddressResponse;
import com.furnisight.user.application.common.port.in.UseCase;

public interface UpdateAddressUseCase extends UseCase<UpdateAddressCommand, AddressResponse> {
}
