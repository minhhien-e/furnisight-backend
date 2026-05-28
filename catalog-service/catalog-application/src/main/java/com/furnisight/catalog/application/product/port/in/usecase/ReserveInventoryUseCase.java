package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.common.port.in.UseCase;
import com.furnisight.catalog.application.product.dto.command.UpdateInventoryCommand;

public interface ReserveInventoryUseCase extends UseCase<UpdateInventoryCommand, Void> {
}
