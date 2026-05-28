package com.furniro.MessageService.service.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.MessageService.database.entity.Notification;
import com.furniro.MessageService.database.repository.NotificationRepository;
import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.API.ApiType;
import com.furniro.MessageService.dto.req.Notify.NotificationReq;
import com.furniro.MessageService.exception.imp.NotifyException;
import com.furniro.MessageService.util.error.NotificationErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public ResponseEntity<AType> getAllNotifications
        (Integer receiverID,
         Integer page,
         Integer size,
         String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        Page<Notification> notifications = notificationRepository.findByUserID(receiverID, pageable);

        if (notifications.isEmpty()) {
            throw new NotifyException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }

        return ResponseEntity.ok(ApiType.success(notifications));
    }

    public ResponseEntity<AType> createNotification
    (NotificationReq req) {
            
        // create notify
        Notification notification = Notification.builder()
                .userID(req.getUserID())
                .title(req.getTitle())
                .content(req.getMessage())
                .type(req.getType())
                .build();

        // save notify
        notificationRepository.save(notification);

        // return response
        return ResponseEntity.ok(ApiType.success(notification));
    }

    public ResponseEntity<AType> readNotification
    (Integer id) {
            
        // find notify
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                new NotifyException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
                        
        // set read true
        notification.setIsRead(true);

        // save notify
        notificationRepository.save(notification);
        
        // return response
        return ResponseEntity.ok(ApiType.success(notification));
    }

}
