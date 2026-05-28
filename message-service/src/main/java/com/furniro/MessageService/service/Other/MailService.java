package com.furniro.MessageService.service.Other;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;


    @Async
    public void sendMailActive(
        String email,
        String fullName,
        String accountID) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String activationLink = "http://localhost:8000/api/v1/furniro/account/active?id=" + accountID;

            String content = "<h3>Chào " + fullName + ",</h3>"
                    + "<p>Vui lòng click vào link bên dưới để kích hoạt tài khoản:</p>"
                    + "<a href='" + activationLink + "'>Kích hoạt ngay</a>";

            helper.setTo(email);
            helper.setSubject("Kích hoạt tài khoản của bạn");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email kích hoạt đã được gửi tới: {}", email);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi mail: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email kích hoạt.");
        }
    }

    @Async
    public void sendMailOTP(
        String email,
        String username,
        String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String content = "<h3>Xin chào " + username + ",</h3>"
                    + "<p>Đây là OTP của bạn : " + otp + "</p>"
                    + "<p> Vui lòng không gửi cho bất kỳ ai </p>";

            helper.setTo(email);
            helper.setSubject("OTP xác thực quên mật khẩu");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email đã gửi OTP đến người dùng : {}", email);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi mail: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email quên mật khẩu.");
        }
    }

    @Async
    public void sendMailPromotion(
        String email,
        String fullName,
        String title,
        String description,
        String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String content = "<h3>Xin chào " + fullName + ",</h3>"
                    + "<p>Đây là mã khuyến mãi của bạn : " + code + "</p>"
                    + "<p>" + description + "</p>";

            helper.setTo(email);
            helper.setSubject(title);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email đã gửi mã khuyến mãi đến người dùng : {}", email);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi mail: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email mã khuyến mãi.");
        }
    }

    @Async
    public void sendMailSubscription(
        String email,
            String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String content = "<h3>Xin chào " + fullName + ",</h3>"
                    + "<p>Cảm ơn bạn đã đăng ký nhận bản tin của chúng tôi.</p>"
                    + "<p>Chúng tôi sẽ gửi cho bạn những thông tin mới nhất về sản phẩm và chương trình khuyến mãi.</p>";

            helper.setTo(email);
            helper.setSubject("Đăng ký nhận bản tin thành công");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email đã gửi bản tin đến người dùng : {}", email);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi mail: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email bản tin.");
        }
    }

}
