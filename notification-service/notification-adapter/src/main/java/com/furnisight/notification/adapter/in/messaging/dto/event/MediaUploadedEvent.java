package com.furnisight.notification.adapter.in.messaging.dto.event;

import lombok.Data;

import java.util.UUID;

@Data
public class MediaUploadedEvent {
    private String mediaName;
    private UUID receiverId;
}
