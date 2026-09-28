package com.studiolynk.model.dto;

import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequirementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class WorkRequirementSummaryDto {

    private Long id;
    private Long studioId;
    private String studioName;
    private String studioLogoUrl;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String formattedTime;
    private String location;
    private DayType dayType;
    private BigDecimal budget;
    private RequirementStatus status;
    private int skillsCount;
    private int servicesCount;
    private int equipmentCount;
    private Long confirmedFreelancerId;
    private String confirmedFreelancerName;

    public WorkRequirementSummaryDto() {
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

    public RequirementStatus getStatus() {
        return status;
    }

    public void setStatus(RequirementStatus status) {
        this.status = status;
    }

    public int getSkillsCount() {
        return skillsCount;
    }

    public void setSkillsCount(int skillsCount) {
        this.skillsCount = skillsCount;
    }

    public int getServicesCount() {
        return servicesCount;
    }

    public void setServicesCount(int servicesCount) {
        this.servicesCount = servicesCount;
    }

    public int getEquipmentCount() {
        return equipmentCount;
    }

    public void setEquipmentCount(int equipmentCount) {
        this.equipmentCount = equipmentCount;
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
}
