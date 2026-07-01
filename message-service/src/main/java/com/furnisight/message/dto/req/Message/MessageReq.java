package com.furnisight.message.dto.req.Message;

import com.furnisight.message.util.enums.MessageType;
import com.furnisight.message.dto.MessageAttachment;

import java.util.List;
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
    private List<MessageAttachment> attachments;
    private Boolean isInternal;
}
