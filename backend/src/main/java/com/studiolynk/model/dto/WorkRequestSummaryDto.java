package com.studiolynk.model.dto;

import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class WorkRequestSummaryDto {

    private Long id;
    private Long requirementId;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String formattedTime;
    private String location;
    private DayType dayType;
    private BigDecimal budget;

    private Long freelancerId;
    private String freelancerName;
    private String freelancerPhotoUrl;
    private String freelancerCity;
    private String freelancerPrimarySkill;

    private Long studioId;
    private String studioName;
    private String studioLogoUrl;

    private RequestStatus status;
    private BigDecimal agreedPrice;
    private String message;
    private String cancellationReason;
    private Instant createdAt;

    public WorkRequestSummaryDto() {
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
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

    public String getFreelancerCity() {
        return freelancerCity;
    }

    public void setFreelancerCity(String freelancerCity) {
        this.freelancerCity = freelancerCity;
    }

    public String getFreelancerPrimarySkill() {
        return freelancerPrimarySkill;
    }

    public void setFreelancerPrimarySkill(String freelancerPrimarySkill) {
        this.freelancerPrimarySkill = freelancerPrimarySkill;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
