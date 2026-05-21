package com.furnisight.media.core.publisher;

import com.furnisight.media.core.model.event.InternalEvent;

public interface InternalEventPublisher {
    void publish(InternalEvent event);
}
