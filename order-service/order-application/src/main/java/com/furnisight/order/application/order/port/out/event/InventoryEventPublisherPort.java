package com.furnisight.order.application.order.port.out.event;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

public interface InventoryEventPublisherPort {
    void publishStockReserveEvent(String orderCode, List<StockItem> items);
    void publishStockReleaseEvent(String orderCode, List<StockItem> items);

    @Data
    @Builder
    class StockItem {
        private UUID productId;
        private UUID variantId;
        private int quantity;
    }
}
