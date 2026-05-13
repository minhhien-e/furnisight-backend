package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;
import java.util.Collections;

public class MissingProductVariantException extends BaseException {
    public MissingProductVariantException(String message) {
        super(ErrorCode.MISSING_PRODUCT_VARIANT, message, Collections.emptyMap());
    }
}
