package com.studiolynk.model.dto;

import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WorkRequestResponseDto {

    private Long id;
    private Long requirementId;
    private Long freelancerId;
    private String freelancerName;
    private String freelancerPhotoUrl;
    private String freelancerPhone;

    private RequestStatus status;
    private BigDecimal agreedPrice;
    private String message;
    private String cancellationReason;

    // Studio details
    private Long studioId;
    private String studioName;
    private String studioLogoUrl;
    private String studioPhone;

    // Requirement details
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String formattedTime;
    private String location;
    private DayType dayType;
    private BigDecimal budget;
    private String description;

    // Protected client contact info (REQ-002, REQ-006)
    private String eventContactName;
    private String eventContactPhone;
    private boolean hasPrivateContactDetails;
    private boolean privateDetailsRevealed;

    private List<SkillDto> requiredSkills = new ArrayList<>();
    private List<ServiceDto> requiredServices = new ArrayList<>();
    private List<EquipmentDto> requiredEquipment = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    public WorkRequestResponseDto() {
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

    public String getFreelancerPhone() {
        return freelancerPhone;
    }

    public void setFreelancerPhone(String freelancerPhone) {
        this.freelancerPhone = freelancerPhone;
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

    public String getStudioPhone() {
        return studioPhone;
    }

    public void setStudioPhone(String studioPhone) {
        this.studioPhone = studioPhone;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventContactName() {
        return eventContactName;
    }

    public void setEventContactName(String eventContactName) {
        this.eventContactName = eventContactName;
    }

    public String getEventContactPhone() {
        return eventContactPhone;
    }

    public void setEventContactPhone(String eventContactPhone) {
        this.eventContactPhone = eventContactPhone;
    }

    public boolean isHasPrivateContactDetails() {
        return hasPrivateContactDetails;
    }

    public void setHasPrivateContactDetails(boolean hasPrivateContactDetails) {
        this.hasPrivateContactDetails = hasPrivateContactDetails;
    }

    public boolean isPrivateDetailsRevealed() {
        return privateDetailsRevealed;
    }

    public void setPrivateDetailsRevealed(boolean privateDetailsRevealed) {
        this.privateDetailsRevealed = privateDetailsRevealed;
    }

    public List<SkillDto> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<SkillDto> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<ServiceDto> getRequiredServices() {
        return requiredServices;
    }

    public void setRequiredServices(List<ServiceDto> requiredServices) {
        this.requiredServices = requiredServices;
    }

    public List<EquipmentDto> getRequiredEquipment() {
        return requiredEquipment;
    }

    public void setRequiredEquipment(List<EquipmentDto> requiredEquipment) {
        this.requiredEquipment = requiredEquipment;
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
