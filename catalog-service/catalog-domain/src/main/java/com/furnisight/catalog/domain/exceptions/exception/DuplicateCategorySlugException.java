package com.furnisight.catalog.domain.exceptions;

public class DuplicateCategorySlugException extends RuntimeException {
    public DuplicateCategorySlugException(String message) {
        super(message);
    }
}
