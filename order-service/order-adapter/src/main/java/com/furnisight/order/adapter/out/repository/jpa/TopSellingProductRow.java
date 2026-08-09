package com.furnisight.order.adapter.out.repository.jpa;

public interface TopSellingProductRow {
    String getProductId();
    String getProductName();
    String getCategoryName();
    String getImageUrl();
    Double getPrice();
    Number getSoldCount();
    Double getTotalRevenue();
}
