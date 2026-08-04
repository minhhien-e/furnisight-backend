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
        List<CatalogStockPort.LookupItem> lookupItems = commandItems.stream()
                .map(item -> new CatalogStockPort.LookupItem(item.getProductId(), item.getVariantId()))
                .toList();
        Map<String, CatalogStockPort.ProductItem> productItems = catalogStockPort.getProductItems(lookupItems, "vi");
        
        commandItems.forEach(item -> validateItem(item, productItems));

        List<OrderItemParam> items = commandItems.stream()
                .map(item -> toOrderItemParam(item, productItems))
                .toList();
        List<ValidateComboRequest.Item> comboItems = commandItems.stream()
                .map(item -> toComboItem(item, productItems))
                .toList();

        return OrderItemValidationResult.builder()
                .items(items)
                .comboItems(comboItems)
                .subtotal(pricingService.calculateSubTotal(items))
                .build();
    }

    private OrderItemParam toOrderItemParam(CreateOrderCommand.OrderItemCommand item, Map<String, CatalogStockPort.ProductItem> productItems) {
        CatalogStockPort.ProductItem product = productItems.get(stockKey(item.getProductId(), item.getVariantId()));
        
        com.furnisight.order.domain.valueobjects.ProductDimensions dimensions = null;
        if (product.weight() != null || product.length() != null || product.width() != null || product.height() != null) {
            dimensions = com.furnisight.order.domain.valueobjects.ProductDimensions.builder()
                    .weight(product.weight())
                    .length(product.length())
                    .width(product.width())
                    .height(product.height())
                    .build();
        }

        return OrderItemParam.builder()
                .productId(product.productId())
                .variantId(product.variantId())
                .slug(product.slug())
                .categoryName(item.getCategoryName())
                .productName(product.productName())
                .price(product.price())
                .quantity(item.getQuantity())
                .imageUrl(product.imageUrl())
                .color(product.color())
                .material(product.material())
                .warranty(product.warranty())
                .dimensions(dimensions)
                .build();
    }

    private ValidateComboRequest.Item toComboItem(CreateOrderCommand.OrderItemCommand item, Map<String, CatalogStockPort.ProductItem> productItems) {
        CatalogStockPort.ProductItem product = productItems.get(stockKey(item.getProductId(), item.getVariantId()));
        return ValidateComboRequest.Item.builder()
                .productId(product.productId())
                .variantId(product.variantId())
                .quantity(item.getQuantity())
                .price(product.price())
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateItem(CreateOrderCommand.OrderItemCommand item, Map<String, CatalogStockPort.ProductItem> productItems) {
        if (item == null || isBlank(item.getProductId()) || isBlank(item.getVariantId())) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_INFO);
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ValidationException(ErrorCode.INVALID_QUANTITY);
        }
        
        CatalogStockPort.ProductItem product = productItems.get(stockKey(item.getProductId(), item.getVariantId()));
        if (product == null) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_INFO);
        }
        
        if (product.price() == null || product.price() <= 0.0) {
            throw new ValidationException(ErrorCode.NEGATIVE_PRICE);
        }
        
        if (product.stockQuantity() == null || item.getQuantity() > product.stockQuantity()) {
            throw new ValidationException(ErrorCode.INSUFFICIENT_STOCK);
        }
    }

    private String stockKey(String productId, String variantId) {
        return normalize(productId) + "::" + normalize(variantId);
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
