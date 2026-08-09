package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.RecordProductFavoriteLogCommand;
import com.furnisight.catalog.application.product.port.in.usecase.RemoveProductFavoriteLogUseCase;
import com.furnisight.catalog.domain.repository.ProductFavoriteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveProductFavoriteLogService implements RemoveProductFavoriteLogUseCase {
    private final ProductFavoriteLogRepository repository;

    @Override
    @Transactional
    public void execute(RecordProductFavoriteLogCommand command) {
        repository.deleteByUserIdAndProductId(command.getUserId(), command.getProductId());
    }
}
