package com.furniro.MessageService.dto.req.Mail;

import lombok.Data;

@Data

public class MailActiveReq {
    private String email;
    private String lastName;
    private String firstName;
    private String accountID;
}
