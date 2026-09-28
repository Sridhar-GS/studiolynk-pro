package com.studiolynk.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.studiolynk.model.enums.NotificationType;
import java.time.LocalDateTime;

public class NotificationDto {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedEntityId;

    @JsonProperty("isRead")
    private boolean isRead;

    private LocalDateTime createdAt;
    private String linkUrl;

    public NotificationDto() {}

    public NotificationDto(Long id, Long userId, NotificationType type, String title, String message,
                           Long relatedEntityId, boolean isRead, LocalDateTime createdAt, String linkUrl) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.linkUrl = linkUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    @JsonProperty("isRead")
    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
}
