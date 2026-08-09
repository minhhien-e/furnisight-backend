package com.furnisight.catalog.domain.valueobjects.product;

import java.util.HashMap;
import java.util.Map;

public class VariantSpecifications extends HashMap<String, Object> implements java.io.Serializable {
    
    public VariantSpecifications() {
        super();
    }

    public VariantSpecifications(Map<String, Object> map) {
        super();
        if (map != null) {
            this.putAll(map);
        }
    }
}
