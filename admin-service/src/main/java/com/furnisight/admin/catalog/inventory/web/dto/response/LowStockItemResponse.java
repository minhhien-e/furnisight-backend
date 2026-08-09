package com.furnisight.admin.catalog.inventory.web.dto.response;

public record LowStockItemResponse(String name, String category, int stock, String level) {
}
