package com.furnisight.notification.adapter.out.sender;

import com.furnisight.notification.adapter.config.EmailProperties;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.out.sender.NotificationSender;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {
    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(SendNotificationCommand command) {
        String email = command.getDestination();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(email);
            helper.setSubject(command.getTitle());
            helper.setText(buildEmailBody(command.getBody(), command.getMetadata()), true);

            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Appends a styled voucher card at the end of the email body
     * when the notification carries voucher metadata.
     */
    private String buildEmailBody(String originalBody, Map<String, Object> metadata) {
        String baseBody = originalBody != null ? originalBody : "";

        if (metadata == null || !metadata.containsKey("voucherId")) {
            return baseBody;
        }

        String voucherCard = buildVoucherCardHtml(metadata);

        // If body is already HTML, inject card before </body> if present,
        // otherwise just append
        if (baseBody.contains("</body>")) {
            return baseBody.replace("</body>", voucherCard + "</body>");
        }
        return baseBody + voucherCard;
    }

    private String buildVoucherCardHtml(Map<String, Object> metadata) {
        String code        = str(metadata.get("voucherCode"));
        String name        = metadata.get("voucherName") != null ? str(metadata.get("voucherName")) : "";
        String discount    = formatDiscount(metadata);
        String minOrder    = metadata.get("minOrder") != null
                ? "Đơn tối thiểu " + formatMoney(metadata.get("minOrder")) + "đ"
                : "Không yêu cầu đơn tối thiểu";
        String expiry      = metadata.get("validUntil") != null
                ? "HSD: " + str(metadata.get("validUntil")).substring(0, 10).replace("-", "/")
                : "";

        return """
                <div style="max-width:560px;margin:32px auto 0;font-family:'Segoe UI',Arial,sans-serif;">
                  <div style="border:1.5px dashed #c9953a;border-radius:14px;overflow:hidden;background:linear-gradient(135deg,#fffbf2,#fff8ea);display:table;width:100%%;box-sizing:border-box;">
                    <div style="display:table-cell;vertical-align:middle;padding:14px 16px;width:60px;">
                      <div style="width:52px;height:52px;border-radius:12px;background:#faecd6;color:#8a5c00;display:inline-block;text-align:center;line-height:52px;font-weight:bold;font-size:26px;">
                        %%
                      </div>
                    </div>
                    <div style="display:table-cell;vertical-align:middle;padding:14px 16px 14px 0;">
                      <div style="font-size:16px;font-weight:700;color:#1a2332;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">%s</div>
                      <div style="margin-top:2px;font-size:14px;font-weight:800;color:#c9953a;letter-spacing:.08em;">%s</div>
                      <div style="margin-top:4px;font-size:18px;font-weight:800;color:#b8630a;">%s</div>
                      <div style="margin-top:2px;font-size:12px;color:#8a7a68;">%s</div>
                      %s
                    </div>
                    <div style="display:table-cell;vertical-align:middle;padding:14px;width:30px;border-left:2px dashed #e8d4aa;text-align:center;">
                      <div style="font-size:10px;font-weight:900;letter-spacing:.12em;color:#c9953a;writing-mode:vertical-rl;transform:rotate(180deg);opacity:0.6;">VOUCHER</div>
                    </div>
                  </div>
                </div>
                """.formatted(
                name.isEmpty() ? "Voucher Khuyến Mãi" : name,
                code,
                discount,
                minOrder,
                expiry.isBlank() ? "" : "<div style='margin-top:2px;font-size:12px;color:#be123c;font-weight:600;'>📅 " + expiry + "</div>"
        );
    }

    private String formatDiscount(Map<String, Object> metadata) {
        String type  = str(metadata.get("discountType"));
        Object value = metadata.get("discountValue");
        
        String valStr = str(value);
        if (valStr.endsWith(".0")) {
            valStr = valStr.substring(0, valStr.length() - 2);
        }

        if ("PERCENT".equals(type) || "PERCENTAGE".equals(type))    return "-" + valStr + "%";
        if ("FIXED".equals(type) || "FIXED_AMOUNT".equals(type))  return "-" + formatMoney(value) + "đ";
        if ("SHIPPING_CAP".equals(type) || "FREE_SHIPPING".equals(type)) return "Miễn phí vận chuyển";
        return valStr;
    }

    private String formatMoney(Object value) {
        if (value == null) return "0";
        try {
            long amount = ((Number) value).longValue();
            return String.format("%,d", amount).replace(",", ".");
        } catch (Exception e) {
            String valStr = str(value);
            if (valStr.endsWith(".0")) valStr = valStr.substring(0, valStr.length() - 2);
            return valStr;
        }
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
