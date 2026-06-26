package com.furniro.MessageService.dto.req.Message;

import com.furniro.MessageService.util.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReq {
    private Integer conversationId;
    private String content;
    private Integer receiverId;
    private Integer senderId;
    private MessageType messageType;
    private Integer fileId;
    private String mediaId;
    private String attachmentUrl;
    private String attachmentName;
    private String attachmentType;
    private Long attachmentSize;
    private Boolean isInternal;
}
