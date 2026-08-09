package com.furnisight.catalog.application.product.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchProductsQuery implements Serializable {
    private String lang;
    private String q;
    private String category;
    private String roomType;
    private String sort;
    private List<String> priceBands;
    private List<Double> priceSliderPct;
    private List<String> materials;
    private List<String> colors;
    private Integer minStar;
    private Boolean saleOnly;
    private String status;
    private int page;
    private int size;
}
