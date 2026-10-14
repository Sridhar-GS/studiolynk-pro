package com.studiolynk.model.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for Work Requirements monitoring in Admin panel (ADM-001, ADM-003).
 */
public class AdminRequirementItemDto {

    private Long id;
    private Long studioId;
    private String studioName;
    private String studioEmail;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String dayType;
    private BigDecimal budget;
    private String status;
    private String description;
    private Long confirmedFreelancerId;
    private String confirmedFreelancerName;
    private long requestCount;
    private List<String> requiredSkills;
    private List<String> requiredServices;
    private Instant createdAt;

    public AdminRequirementItemDto() {
    }

    public AdminRequirementItemDto(Long id, Long studioId, String studioName, String studioEmail,
                                   String eventName, String eventType, LocalDate eventDate,
                                   LocalTime startTime, LocalTime endTime, String location,
                                   String dayType, BigDecimal budget, String status,
                                   String description, Long confirmedFreelancerId,
                                   String confirmedFreelancerName, long requestCount,
                                   List<String> requiredSkills, List<String> requiredServices,
                                   Instant createdAt) {
        this.id = id;
        this.studioId = studioId;
        this.studioName = studioName;
        this.studioEmail = studioEmail;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.dayType = dayType;
        this.budget = budget;
        this.status = status;
        this.description = description;
        this.confirmedFreelancerId = confirmedFreelancerId;
        this.confirmedFreelancerName = confirmedFreelancerName;
        this.requestCount = requestCount;
        this.requiredSkills = requiredSkills;
        this.requiredServices = requiredServices;
        this.createdAt = createdAt;
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

    public String getStudioEmail() {
        return studioEmail;
    }

    public void setStudioEmail(String studioEmail) {
        this.studioEmail = studioEmail;
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

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<String> getRequiredServices() {
        return requiredServices;
    }

    public void setRequiredServices(List<String> requiredServices) {
        this.requiredServices = requiredServices;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
