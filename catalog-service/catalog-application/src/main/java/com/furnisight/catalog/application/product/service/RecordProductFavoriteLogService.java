package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.RecordProductFavoriteLogCommand;
import com.furnisight.catalog.application.product.port.in.usecase.RecordProductFavoriteLogUseCase;
import com.furnisight.catalog.domain.entities.ProductFavoriteLog;
import com.furnisight.catalog.domain.repository.ProductFavoriteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordProductFavoriteLogService implements RecordProductFavoriteLogUseCase {
    private final ProductFavoriteLogRepository repository;

    @Override
    @Transactional
    public void execute(RecordProductFavoriteLogCommand command) {
        ProductFavoriteLog log = new ProductFavoriteLog(command.getUserId(), command.getProductId());
        repository.save(log);
    }
}
