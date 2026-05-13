package com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
