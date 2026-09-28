package com.studiolynk.service.impl;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.NotificationDto;
import com.studiolynk.model.entity.Notification;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.NotificationType;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.NotificationRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    @Transactional
    public NotificationDto createNotification(User recipient, NotificationType type, String title, String message, Long relatedEntityId) {
        if (recipient == null) {
            log.warn("Cannot create notification for null recipient");
            return null;
        }

        Notification notification = new Notification(recipient, type, title, message, relatedEntityId);
        Notification saved = notificationRepository.save(notification);

        NotificationDto dto = mapToDto(saved);

        // Real-time notification dispatch over STOMP broker
        try {
            simpMessagingTemplate.convertAndSend("/topic/notifications." + recipient.getId(), dto);
        } catch (Exception e) {
            log.warn("Failed to dispatch live notification [{}] over WebSocket: {}", saved.getId(), e.getMessage());
        }

        log.info("Created notification [{}] type [{}] for user [{}]", saved.getId(), type, recipient.getEmail());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(String userEmail, Boolean unreadOnly) {
        User user = getUserByEmail(userEmail);
        List<Notification> notifications;

        if (Boolean.TRUE.equals(unreadOnly)) {
            notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User user = getUserByEmail(userEmail);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(String userEmail, Long notificationId) {
        User user = getUserByEmail(userEmail);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = getUserByEmail(userEmail);
        int count = notificationRepository.markAllAsReadByUserId(user.getId());
        log.info("Marked [{}] notifications as read for user [{}]", count, userEmail);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private NotificationDto mapToDto(Notification n) {
        String linkUrl = computeLinkUrl(n);
        return new NotificationDto(
                n.getId(),
                n.getUser().getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getRelatedEntityId(),
                n.isRead(),
                n.getCreatedAt(),
                linkUrl
        );
    }

    private String computeLinkUrl(Notification n) {
        if (n.getType() == null) return "/";
        UserRole role = n.getUser() != null ? n.getUser().getRole() : null;
        Long entityId = n.getRelatedEntityId();

        switch (n.getType()) {
            case REQUEST_RECEIVED:
            case STUDIO_CONFIRMATION:
                return entityId != null ? "/freelancer/requests/" + entityId : "/freelancer/requests";

            case REQUEST_ACCEPTED:
            case REQUEST_REJECTED:
                return "/studio/requests";

            case NEW_MESSAGE:
                return "/messages";

            case WORK_STARTED:
            case WORK_COMPLETED:
            case RATING_REMINDER:
                if (role == UserRole.STUDIO) {
                    return entityId != null ? "/studio/requirements/" + entityId : "/studio/requirements";
                } else {
                    return entityId != null ? "/freelancer/requests/" + entityId : "/freelancer/requests";
                }

            case WORK_CANCELLED:
                if (role == UserRole.STUDIO) {
                    return entityId != null ? "/studio/requirements/" + entityId : "/studio/requirements";
                } else {
                    return "/freelancer/requests";
                }

            default:
                return "/";
        }
    }
}
