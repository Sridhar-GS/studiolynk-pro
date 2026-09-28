package com.studiolynk.model.dto;

import com.studiolynk.model.enums.AvailabilityStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityCheckResponseDto {

    private Long freelancerId;
    private LocalDate date;
    private LocalTime requestedStartTime;
    private LocalTime requestedEndTime;
    private boolean withinWindow;
    private AvailabilityStatus status;
    private LocalTime availableStartTime;
    private LocalTime availableEndTime;
    private boolean match;
    private String reason;

    public AvailabilityCheckResponseDto() {
    }

    public AvailabilityCheckResponseDto(Long freelancerId, LocalDate date, LocalTime requestedStartTime,
                                       LocalTime requestedEndTime, boolean withinWindow, AvailabilityStatus status,
                                       LocalTime availableStartTime, LocalTime availableEndTime,
                                       boolean match, String reason) {
        this.freelancerId = freelancerId;
        this.date = date;
        this.requestedStartTime = requestedStartTime;
        this.requestedEndTime = requestedEndTime;
        this.withinWindow = withinWindow;
        this.status = status;
        this.availableStartTime = availableStartTime;
        this.availableEndTime = availableEndTime;
        this.match = match;
        this.reason = reason;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getRequestedStartTime() {
        return requestedStartTime;
    }

    public void setRequestedStartTime(LocalTime requestedStartTime) {
        this.requestedStartTime = requestedStartTime;
    }

    public LocalTime getRequestedEndTime() {
        return requestedEndTime;
    }

    public void setRequestedEndTime(LocalTime requestedEndTime) {
        this.requestedEndTime = requestedEndTime;
    }

    public boolean isWithinWindow() {
        return withinWindow;
    }

    public void setWithinWindow(boolean withinWindow) {
        this.withinWindow = withinWindow;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilityStatus status) {
        this.status = status;
    }

    public LocalTime getAvailableStartTime() {
        return availableStartTime;
    }

    public void setAvailableStartTime(LocalTime availableStartTime) {
        this.availableStartTime = availableStartTime;
    }

    public LocalTime getAvailableEndTime() {
        return availableEndTime;
    }

    public void setAvailableEndTime(LocalTime availableEndTime) {
        this.availableEndTime = availableEndTime;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
