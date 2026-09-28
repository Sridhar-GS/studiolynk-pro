package com.studiolynk.model.dto;

import com.studiolynk.model.enums.RequirementStatus;

import java.time.Instant;
import java.time.LocalDate;

public class ConversationDto {

    private Long id;

    // Requirement context
    private Long requirementId;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private RequirementStatus requirementStatus;

    // Studio details
    private Long studioId;
    private String studioName;
    private String studioLogoUrl;

    // Freelancer details
    private Long freelancerId;
    private String freelancerName;
    private String freelancerPhotoUrl;

    // Recent activity & counters
    private String lastMessage;
    private Instant lastMessageAt;
    private Long lastMessageSenderId;
    private long unreadCount;
    private Instant createdAt;

    public ConversationDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public RequirementStatus getRequirementStatus() {
        return requirementStatus;
    }

    public void setRequirementStatus(RequirementStatus requirementStatus) {
        this.requirementStatus = requirementStatus;
    }

    public Long getStudioId() {
        return studioId;
    }

    public void setStudioId(Long studioId) {
        this.studioId = studioId;
    }

    public String getStudioName() {
        return studioName;
    }

    public void setStudioName(String studioName) {
        this.studioName = studioName;
    }

    public String getStudioLogoUrl() {
        return studioLogoUrl;
    }

    public void setStudioLogoUrl(String studioLogoUrl) {
        this.studioLogoUrl = studioLogoUrl;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }

    public String getFreelancerPhotoUrl() {
        return freelancerPhotoUrl;
    }

    public void setFreelancerPhotoUrl(String freelancerPhotoUrl) {
        this.freelancerPhotoUrl = freelancerPhotoUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Long getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(Long lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
