package com.furnisight.promotion.domain.valueobjects;

public record EmailAddress(String value) {
    
    public boolean isValid() {
        if (value == null) return false;
        String email = value.trim();
        int at = email.indexOf('@');
        return at > 0 && at < email.length() - 3 && email.indexOf('.', at) > at + 1;
    }
    
    public String getCleanValue() {
        return isValid() ? value.trim() : null;
    }
}
