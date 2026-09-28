package com.studiolynk.model.dto;

import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequirementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WorkRequirementResponseDto {

    private Long id;
    private Long studioId;
    private String studioName;
    private String studioLogoUrl;
    private String studioPhone;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String formattedTime;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private DayType dayType;
    private BigDecimal budget;
    private String description;
    private RequirementStatus status;

    // Protected private event contact details (WRK-002, REQ-002, REQ-006)
    // Only populated when viewed by the owning Studio or the confirmed Freelancer
    private String eventContactName;
    private String eventContactPhone;
    private boolean hasPrivateContactDetails;
    private boolean privateDetailsRevealed;

    private Long confirmedFreelancerId;
    private String confirmedFreelancerName;
    private String confirmedFreelancerPhotoUrl;

    private List<SkillDto> requiredSkills = new ArrayList<>();
    private List<ServiceDto> requiredServices = new ArrayList<>();
    private List<EquipmentDto> requiredEquipment = new ArrayList<>();

    private int activeRequestsCount;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkRequirementResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
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

    public RequirementStatus getStatus() {
        return status;
    }

    public void setStatus(RequirementStatus status) {
        this.status = status;
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

    public Long getConfirmedFreelancerId() {
        return confirmedFreelancerId;
    }

    public void setConfirmedFreelancerId(Long confirmedFreelancerId) {
        this.confirmedFreelancerId = confirmedFreelancerId;
    }

    public String getConfirmedFreelancerName() {
        return confirmedFreelancerName;
    }

    public void setConfirmedFreelancerName(String confirmedFreelancerName) {
        this.confirmedFreelancerName = confirmedFreelancerName;
    }

    public String getConfirmedFreelancerPhotoUrl() {
        return confirmedFreelancerPhotoUrl;
    }

    public void setConfirmedFreelancerPhotoUrl(String confirmedFreelancerPhotoUrl) {
        this.confirmedFreelancerPhotoUrl = confirmedFreelancerPhotoUrl;
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

    public int getActiveRequestsCount() {
        return activeRequestsCount;
    }

    public void setActiveRequestsCount(int activeRequestsCount) {
        this.activeRequestsCount = activeRequestsCount;
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
