package com.studiolynk.model.dto.admin;

import java.time.LocalDateTime;

/**
 * DTO for platform activity audit events displayed in the Admin panel (ADM-001, ADM-003).
 */
public class AdminActivityItemDto {

    private Long id;
    private LocalDateTime timestamp;
    private String type;
    private String title;
    private String description;
    private String actorEmail;
    private String actorRole;
    private String targetEntity;
    private Long targetId;

    public AdminActivityItemDto() {
    }

    public AdminActivityItemDto(Long id, LocalDateTime timestamp, String type, String title,
                                String description, String actorEmail, String actorRole,
                                String targetEntity, Long targetId) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.title = title;
        this.description = description;
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}
