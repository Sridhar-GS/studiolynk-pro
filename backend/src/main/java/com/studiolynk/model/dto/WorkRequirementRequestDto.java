package com.studiolynk.model.dto;

import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequirementStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WorkRequirementRequestDto {

    @NotBlank(message = "Event name is required")
    @Size(max = 150, message = "Event name cannot exceed 150 characters")
    private String eventName;

    @NotBlank(message = "Event type is required (e.g. Wedding, Reception, Commercial, Fashion)")
    @Size(max = 100, message = "Event type cannot exceed 100 characters")
    private String eventType;

    @NotNull(message = "Event date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;

    @NotBlank(message = "Location/venue address is required")
    private String location;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private DayType dayType = DayType.FULL_DAY;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.00", message = "Budget must be a non-negative amount")
    private BigDecimal budget;

    private String description;

    private RequirementStatus status = RequirementStatus.OPEN;

    // Private event-person contact details (WRK-002, REQ-002, REQ-006)
    @Size(max = 150, message = "Event contact name cannot exceed 150 characters")
    private String eventContactName;

    @Size(max = 50, message = "Event contact phone cannot exceed 50 characters")
    private String eventContactPhone;

    private List<Long> requiredSkillIds = new ArrayList<>();
    private List<Long> requiredServiceIds = new ArrayList<>();
    private List<Long> requiredEquipmentIds = new ArrayList<>();

    public WorkRequirementRequestDto() {
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

    public List<Long> getRequiredSkillIds() {
        return requiredSkillIds;
    }

    public void setRequiredSkillIds(List<Long> requiredSkillIds) {
        this.requiredSkillIds = requiredSkillIds;
    }

    public List<Long> getRequiredServiceIds() {
        return requiredServiceIds;
    }

    public void setRequiredServiceIds(List<Long> requiredServiceIds) {
        this.requiredServiceIds = requiredServiceIds;
    }

    public List<Long> getRequiredEquipmentIds() {
        return requiredEquipmentIds;
    }

    public void setRequiredEquipmentIds(List<Long> requiredEquipmentIds) {
        this.requiredEquipmentIds = requiredEquipmentIds;
    }
}
