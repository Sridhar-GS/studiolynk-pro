package com.studiolynk.service;

import com.studiolynk.model.dto.NotificationDto;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationDto createNotification(User recipient, NotificationType type, String title, String message, Long relatedEntityId);

    List<NotificationDto> getUserNotifications(String userEmail, Boolean unreadOnly);

    long getUnreadCount(String userEmail);

    NotificationDto markAsRead(String userEmail, Long notificationId);

    void markAllAsRead(String userEmail);
}
