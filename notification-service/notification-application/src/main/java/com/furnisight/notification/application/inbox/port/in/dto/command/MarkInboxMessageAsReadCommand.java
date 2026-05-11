package com.furnisight.notification.application.inbox.port.in.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkInboxMessageAsReadCommand {
    private UUID messageId;
}
