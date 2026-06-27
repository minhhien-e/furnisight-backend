package com.furniro.MessageService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAttachment {
    private String mediaId;
    private String url;
    private String name;
    private String type;
    private Long size;
    private Boolean isImage;
}
