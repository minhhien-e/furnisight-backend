package com.furnisight.media.infrastructure.publisher;

import com.furnisight.media.core.model.event.InternalEvent;
import com.furnisight.media.core.publisher.InternalEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringInternalEventPublisher implements InternalEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(InternalEvent event) {
        publisher.publishEvent(event);
    }
}

