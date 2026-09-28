package com.studiolynk.model.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityCheckRequestDto {

    private Long freelancerId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    public AvailabilityCheckRequestDto() {
    }

    public AvailabilityCheckRequestDto(Long freelancerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.freelancerId = freelancerId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
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
}
