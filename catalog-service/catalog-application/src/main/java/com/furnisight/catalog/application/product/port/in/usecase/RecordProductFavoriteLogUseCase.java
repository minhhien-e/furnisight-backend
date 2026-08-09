package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.RecordProductFavoriteLogCommand;

public interface RecordProductFavoriteLogUseCase {
    void execute(RecordProductFavoriteLogCommand command);
}
