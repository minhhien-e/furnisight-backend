package com.furnisight.notification.domain.exception;

import com.furnisight.notification.domain.exception.enums.ErrorCode;
import com.furnisight.notification.domain.model.enums.NotificationChannel;

import java.util.Map;

public class UnsupportedNotificationChannelException extends BaseException {

    public UnsupportedNotificationChannelException(NotificationChannel channel) {
        super(ErrorCode.UNSUPPORTED_NOTIFICATION_CHANNEL,
              "Unsupported notification channel: " + channel,
              Map.of("channel", channel));
    }
}
