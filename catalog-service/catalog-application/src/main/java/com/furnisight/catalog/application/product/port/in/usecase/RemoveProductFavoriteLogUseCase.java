package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.RecordProductFavoriteLogCommand;

public interface RemoveProductFavoriteLogUseCase {
    void execute(RecordProductFavoriteLogCommand command);
}
