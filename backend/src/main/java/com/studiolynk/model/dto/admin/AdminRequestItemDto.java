package com.studiolynk.model.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for Work Requests / proposals monitoring in Admin panel (ADM-001, ADM-003).
 */
public class AdminRequestItemDto {

    private Long id;
    private Long requirementId;
    private String eventName;
    private LocalDate eventDate;
    private Long studioId;
    private String studioName;
    private Long freelancerId;
    private String freelancerName;
    private String freelancerEmail;
    private BigDecimal agreedPrice;
    private String status;
    private String message;
    private String cancellationReason;
    private Instant createdAt;
    private Instant updatedAt;

    public AdminRequestItemDto() {
    }

    public AdminRequestItemDto(Long id, Long requirementId, String eventName, LocalDate eventDate,
                               Long studioId, String studioName, Long freelancerId,
                               String freelancerName, String freelancerEmail, BigDecimal agreedPrice,
                               String status, String message, String cancellationReason,
                               Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.requirementId = requirementId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.studioId = studioId;
        this.studioName = studioName;
        this.freelancerId = freelancerId;
        this.freelancerName = freelancerName;
        this.freelancerEmail = freelancerEmail;
        this.agreedPrice = agreedPrice;
        this.status = status;
        this.message = message;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
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

    public String getFreelancerEmail() {
        return freelancerEmail;
    }

    public void setFreelancerEmail(String freelancerEmail) {
        this.freelancerEmail = freelancerEmail;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
