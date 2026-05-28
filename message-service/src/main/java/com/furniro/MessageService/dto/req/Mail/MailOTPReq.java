package com.furniro.MessageService.dto.req.Mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailOTPReq {
    private String email;
    private String userName;
    private String otp;
}
