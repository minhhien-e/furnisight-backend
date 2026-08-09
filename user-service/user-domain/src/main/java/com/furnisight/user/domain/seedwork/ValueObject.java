package com.furnisight.user.domain.seedwork;

import java.util.List;
import java.util.Objects;

public abstract class ValueObject {

    protected abstract List<Object> getEqualityComponents();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValueObject other = (ValueObject) obj;
        return getEqualityComponents().equals(other.getEqualityComponents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEqualityComponents().toArray());
    }
}
