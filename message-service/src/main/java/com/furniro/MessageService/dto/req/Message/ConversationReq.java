package com.furniro.MessageService.dto.req.Message;

import com.furniro.MessageService.util.enums.MessageType;
import com.furniro.MessageService.util.enums.ConversationChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ConversationReq {

    @NotNull(message = "buyerId is required")
    private Integer buyerId;

    private Integer staffId;

    @NotBlank(message = "message is required")
    private String message;

    private MessageType messageType;

    private ConversationChannel channel;

    private Integer fileId;
}
