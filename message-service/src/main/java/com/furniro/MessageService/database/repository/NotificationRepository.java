package com.furniro.MessageService.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furniro.MessageService.database.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    Page<Notification> findByUserID(Integer userID, Pageable pageable);
}
