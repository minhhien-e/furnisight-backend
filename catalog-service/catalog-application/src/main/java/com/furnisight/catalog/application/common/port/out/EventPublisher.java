package com.furnisight.catalog.application.common.port.out;

public interface EventPublisher {
    void publish(String payload, String topic);
}
