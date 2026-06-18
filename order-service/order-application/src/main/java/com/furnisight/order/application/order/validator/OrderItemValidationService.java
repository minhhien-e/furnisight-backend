package com.furnisight.order.application.order.validator;

import com.furnisight.order.application.catalog.port.out.CatalogStockPort;
import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.services.PricingService;
import com.furnisight.order.domain.services.dto.OrderItemParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderItemValidationService {
    private final PricingService pricingService;
    private final CatalogStockPort catalogStockPort;

    public OrderItemValidationResult validate(CreateOrderCommand command) {
        List<CreateOrderCommand.OrderItemCommand> commandItems = command.getItems();
        if (commandItems == null || commandItems.isEmpty()) {
            throw new ValidationException(ErrorCode.ORDER_ITEM_EMPTY);
        }
        commandItems.forEach(this::validateItem);
        validateStock(commandItems);

        List<OrderItemParam> items = commandItems.stream()
                .map(this::toOrderItemParam)
                .toList();
        List<ValidateComboRequest.Item> comboItems = commandItems.stream()
                .map(this::toComboItem)
                .toList();

        return OrderItemValidationResult.builder()
                .items(items)
                .comboItems(comboItems)
                .subtotal(pricingService.calculateSubTotal(items))
                .build();
    }

    private OrderItemParam toOrderItemParam(CreateOrderCommand.OrderItemCommand item) {
        validateItem(item);
        return OrderItemParam.builder()
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .categoryName(item.getCategoryName())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .imageUrl(item.getImageUrl())
                .build();
    }

    private ValidateComboRequest.Item toComboItem(CreateOrderCommand.OrderItemCommand item) {
        validateItem(item);
        return ValidateComboRequest.Item.builder()
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }

    private void validateItem(CreateOrderCommand.OrderItemCommand item) {
        if (item == null || isBlank(item.getProductId()) || isBlank(item.getVariantId())) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_INFO);
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ValidationException(ErrorCode.INVALID_QUANTITY);
        }
        if (item.getPrice() == null || item.getPrice() <= 0.0) {
            throw new ValidationException(ErrorCode.NEGATIVE_PRICE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateStock(List<CreateOrderCommand.OrderItemCommand> commandItems) {
        List<CatalogStockPort.LookupItem> lookupItems = commandItems.stream()
                .map(item -> new CatalogStockPort.LookupItem(item.getProductId(), item.getVariantId()))
                .toList();
        Map<String, CatalogStockPort.StockItem> stockItems = catalogStockPort.getStockItems(lookupItems);

        for (CreateOrderCommand.OrderItemCommand item : commandItems) {
            CatalogStockPort.StockItem stockItem = stockItems.get(stockKey(item.getProductId(), item.getVariantId()));
            Integer stockQuantity = stockItem != null ? stockItem.stockQuantity() : null;
            if (stockQuantity == null || item.getQuantity() > stockQuantity) {
                throw new ValidationException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }
    }

    private String stockKey(String productId, String variantId) {
        return normalize(productId) + "::" + normalize(variantId);
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
