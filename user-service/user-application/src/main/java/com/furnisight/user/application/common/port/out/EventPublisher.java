package com.furnisight.user.application.common.port.out;

public interface EventPublisher {
    void publish(String payload, String topic);
}
